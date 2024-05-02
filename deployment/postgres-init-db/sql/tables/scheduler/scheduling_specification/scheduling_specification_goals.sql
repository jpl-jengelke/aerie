create table scheduler.scheduling_specification_goals (
  specification_id integer not null,
  goal_id integer not null,
  goal_revision integer, -- latest is null
  priority integer not null,
  enabled boolean not null default true,

  simulate_after boolean not null default true,

  constraint scheduling_specification_goals_primary_key
    primary key (specification_id, goal_id),
  constraint scheduling_specification_goals_unique_priorities
    unique (specification_id, priority) deferrable initially deferred,
  constraint scheduling_specification_goals_specification_exists
    foreign key (specification_id)
      references scheduler.scheduling_specification
      on update cascade
      on delete cascade,
  constraint non_negative_specification_goal_priority check (priority >= 0),
  constraint scheduling_spec_goal_exists
    foreign key (goal_id)
      references scheduler.scheduling_goal_metadata
      on update cascade
      on delete restrict,
  constraint scheduling_spec_goal_definition_exists
    foreign key (goal_id, goal_revision)
      references scheduler.scheduling_goal_definition
      on update cascade
      on delete restrict
);

comment on table scheduler.scheduling_specification_goals is e''
  'The scheduling goals to be executed against a given plan.';
comment on column scheduler.scheduling_specification_goals.specification_id is e''
  'The plan scheduling specification this goal is on. Half of the primary key.';
comment on column scheduler.scheduling_specification_goals.goal_id is e''
  'The id of a specific goal in the specification. Half of the primary key.';
comment on column scheduler.scheduling_specification_goals.goal_revision is e''
  'The version of the goal definition to use. Leave NULL to use the latest version.';
comment on column scheduler.scheduling_specification_goals.priority is e''
  'The relative priority of a scheduling goal in relation to other '
  'scheduling goals within the same specification.';
comment on column scheduler.scheduling_specification_goals.enabled is e''
  'Whether to run a given goal. Defaults to TRUE.';
comment on column scheduler.scheduling_specification_goals.simulate_after is e''
  'Whether to re-simulate after evaluating this goal and before the next goal.';

create function scheduler.insert_scheduling_specification_goal_func()
  returns trigger
  language plpgsql as $$
  declare
    next_priority integer;
begin
  select coalesce(
    (select priority
     from scheduler.scheduling_specification_goals ssg
     where ssg.specification_id = new.specification_id
     order by priority desc
     limit 1), -1) + 1
  into next_priority;

  if new.priority > next_priority then
    raise numeric_value_out_of_range using
      message = ('Updated priority % for specification_id % is not consecutive', new.priority, new.specification_id),
      hint = ('The next available priority is %.', next_priority);
  end if;

  if new.priority is null then
    new.priority = next_priority;
  end if;

  update scheduler.scheduling_specification_goals
  set priority = priority + 1
  where specification_id = new.specification_id
    and priority >= new.priority;
  return new;
end;
$$;

comment on function scheduler.insert_scheduling_specification_goal_func is e''
  'Checks that the inserted priority is consecutive, and reorders (increments) higher or equal priorities to make room.';

create trigger insert_scheduling_specification_goal
  before insert on scheduler.scheduling_specification_goals
  for each row
execute function scheduler.insert_scheduling_specification_goal_func();

create function scheduler.update_scheduling_specification_goal_func()
  returns trigger
  language plpgsql as $$
  declare
    next_priority integer;
begin
  select coalesce(
    (select priority
     from scheduler.scheduling_specification_goals ssg
     where ssg.specification_id = new.specification_id
     order by priority desc
     limit 1), -1) + 1
  into next_priority;

  if new.priority > next_priority then
    raise numeric_value_out_of_range using
      message = ('Updated priority % for specification_id % is not consecutive', new.priority, new.specification_id),
      hint = ('The next available priority is %.', next_priority);
  end if;

  if new.priority > old.priority then
    update scheduler.scheduling_specification_goals
    set priority = priority - 1
    where specification_id = new.specification_id
      and priority between old.priority + 1 and new.priority
      and goal_id != new.goal_id;
  else
    update scheduler.scheduling_specification_goals
    set priority = priority + 1
    where specification_id = new.specification_id
      and priority between new.priority and old.priority - 1
      and goal_id != new.goal_id;
  end if;
  return new;
end;
$$;

comment on function scheduler.update_scheduling_specification_goal_func is e''
  'Checks that the updated priority is consecutive, and reorders priorities to make room.';

create trigger update_scheduling_specification_goal
  before update on scheduler.scheduling_specification_goals
  for each row
  when (OLD.priority is distinct from NEW.priority and pg_trigger_depth() < 1)
execute function scheduler.update_scheduling_specification_goal_func();

create function scheduler.delete_scheduling_specification_goal_func()
  returns trigger
  language plpgsql as $$
  declare
    r scheduler.scheduling_specification_goals;
begin
  -- Perform updates in reverse-priority order to ensure that there are no gaps
  for r in select * from removed_rows order by priority desc loop
    update scheduler.scheduling_specification_goals
    set priority = priority - 1
    where specification_id = r.specification_id
      and priority > r.priority;
  end loop;
  return null;
end;
$$;

comment on function scheduler.delete_scheduling_specification_goal_func() is e''
  'Reorders (decrements) priorities to fill the gap from deleted priority.';

create trigger delete_scheduling_specification_goal
  after delete on scheduler.scheduling_specification_goals
  referencing old table as removed_rows
  for each statement
execute function scheduler.delete_scheduling_specification_goal_func();

create function scheduler.increment_spec_revision_on_goal_spec_update()
  returns trigger
  security definer
language plpgsql as $$begin
  update scheduler.scheduling_specification
  set revision = revision + 1
  where id = new.specification_id;
  return new;
end$$;

create trigger increment_revision_on_goal_update
  before insert or update on scheduler.scheduling_specification_goals
  for each row
  when (pg_trigger_depth() < 1)
  execute function scheduler.increment_spec_revision_on_goal_spec_update();

create function scheduler.increment_spec_revision_on_goal_spec_delete()
  returns trigger
  security definer
language plpgsql as $$begin
  update scheduler.scheduling_specification
  set revision = revision + 1
  where id = old.specification_id;
  return old;
end$$;

create trigger increment_revision_on_goal_delete
  before delete on scheduler.scheduling_specification_goals
  for each row
  execute function scheduler.increment_spec_revision_on_goal_spec_delete();
