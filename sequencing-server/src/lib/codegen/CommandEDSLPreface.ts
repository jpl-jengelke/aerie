/** START Preface */

export enum TimingTypes {
  ABSOLUTE = 'ABSOLUTE',
  COMMAND_RELATIVE = 'COMMAND_RELATIVE',
  EPOCH_RELATIVE = 'EPOCH_RELATIVE',
  COMMAND_COMPLETE = 'COMMAND_COMPLETE',
}

// @ts-ignore : 'Args' found in JSON Spec
export type CommandOptions<A extends Args[] | { [argName: string]: any } = [] | {}> = {
  stem: string;
  arguments: A;
  // @ts-ignore : 'Metadata' found in JSON Spec
  metadata?: Metadata | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  description?: Description | undefined;
  // @ts-ignore : 'Model' found in JSON Spec
  models?: Model[] | undefined;
} & (
  | {
      absoluteTime: Temporal.Instant;
    }
  | {
      epochTime: Temporal.Duration;
    }
  | {
      relativeTime: Temporal.Duration;
    }
  // CommandComplete
  | {}
);

// @ts-ignore : 'Args' found in JSON Spec
export type ImmediateOptions<A extends Args[] | { [argName: string]: any } = [] | {}> = {
  stem: string;
  arguments: A;
  // @ts-ignore : 'Metadata' found in JSON Spec
  metadata?: Metadata | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  description?: Description | undefined;
};

export type HardwareOptions = {
  stem: string;
  // @ts-ignore : 'Description' found in JSON Spec
  description?: Description;
  // @ts-ignore : 'Metadata' found in JSON Spec
  metadata?: Metadata;
};

export type GroundOptions = {
  name: string;
  // @ts-ignore : 'Args' found in JSON Spec
  args?: Args;
  // @ts-ignore : 'Description' found in JSON Spec
  description?: Description;
  // @ts-ignore : 'Metadata' found in JSON Spec
  metadata?: Metadata;
  // @ts-ignore : 'Model' found in JSON Spec
  models?: Model[];
} & (
  | {
      absoluteTime: Temporal.Instant;
    }
  | {
      epochTime: Temporal.Duration;
    }
  | {
      relativeTime: Temporal.Duration;
    }
  // CommandComplete
  | {}
);

export type Arrayable<T> = T | Arrayable<T>[];

export interface SequenceOptions {
  seqId: string;
  // @ts-ignore : 'Metadata' found in JSON Spec
  metadata: Metadata;

  // @ts-ignore : 'VariableDeclaration' found in JSON Spec
  locals?: [VariableDeclaration, ...VariableDeclaration[]];
  // @ts-ignore : 'VariableDeclaration' found in JSON Spec
  parameters?: [VariableDeclaration, ...VariableDeclaration[]];
  // @ts-ignore : 'Step' found in JSON Spec
  steps?: Step[];
  // @ts-ignore : 'Request' found in JSON Spec
  requests?: Request[];
  // @ts-ignore : 'ImmediateCommand' found in JSON Spec
  immediate_commands?: ImmediateCommand[];
  // @ts-ignore : 'HardwareCommand' found in JSON Spec
  hardware_commands?: HardwareCommand[];
}

declare global {
  // @ts-ignore : 'SeqJson' found in JSON Spec
  class Sequence implements SeqJson {
    public readonly id: string;
    // @ts-ignore : 'Metadata' found in JSON Spec
    public readonly metadata: Metadata;

    // @ts-ignore : 'VariableDeclaration' found in JSON Spec
    public readonly locals?: [VariableDeclaration, ...VariableDeclaration[]];
    // @ts-ignore : 'VariableDeclaration' found in JSON Spec
    public readonly parameters?: [VariableDeclaration, ...VariableDeclaration[]];
    // @ts-ignore : 'Step' found in JSON Spec
    public readonly steps?: Step[];
    // @ts-ignore : 'Request' found in JSON Spec
    public readonly requests?: Request[];
    // @ts-ignore : 'ImmediateCommand' found in JSON Spec
    public readonly immediate_commands?: ImmediateCommand[];
    // @ts-ignore : 'HardwareCommand' found in JSON Spec
    public readonly hardware_commands?: HardwareCommand[];
    [k: string]: unknown;

    public static new(
      opts:
        | {
            seqId: string;
            // @ts-ignore : 'VariableDeclaration' found in JSON Spec
            locals?: [VariableDeclaration, ...VariableDeclaration[]];
            // @ts-ignore : 'Metadata' found in JSON Spec
            metadata: Metadata;
            // @ts-ignore : 'VariableDeclaration' found in JSON Spec
            parameters?: [VariableDeclaration, ...VariableDeclaration[]];
            // @ts-ignore : 'Step' found in JSON Spec
            steps?: Step[];
            // @ts-ignore : 'Request' found in JSON Spec
            requests?: Request[];
            // @ts-ignore : 'ImmediateCommand' found in JSON Spec
            immediate_commands?: ImmediateCommand[];
            // @ts-ignore : 'HardwareCommand' found in JSON Spec
            hardware_commands?: HardwareCommand[];
          }
        // @ts-ignore : 'SeqJson' found in JSON Spec
        | SeqJson,
    ): Sequence;

    // @ts-ignore : 'SeqJson' found in JSON Spec
    public toSeqJson(): SeqJson;
  }

  // @ts-ignore : 'Args' found in JSON Spec
  class CommandStem<A extends Args[] | { [argName: string]: any } = [] | {}> implements Command {
    // @ts-ignore : 'Args' found in JSON Spec
    args: Args;
    stem: string;
    // @ts-ignore : 'TIME' found in JSON Spec
    time: Time;
    type: 'command';

    public static new<A extends any[] | { [argName: string]: any }>(opts: CommandOptions<A>): CommandStem<A>;

    // @ts-ignore : 'Command' found in JSON Spec
    public toSeqJson(): Command;

    // @ts-ignore : 'Model' found in JSON Spec
    public MODELS(models: Model[]): CommandStem<A>;
    // @ts-ignore : 'Model' found in JSON Spec
    public GET_MODELS(): Model[] | undefined;

    // @ts-ignore : 'Metadata' found in JSON Spec
    public METADATA(metadata: Metadata): CommandStem<A>;
    // @ts-ignore : 'Metadata' found in JSON Spec
    public GET_METADATA(): Metadata | undefined;

    // @ts-ignore : 'Description' found in JSON Spec
    public DESCRIPTION(description: Description): CommandStem<A>;
    // @ts-ignore : 'Description' found in JSON Spec
    public GET_DESCRIPTION(): Description | undefined;
  }

  // @ts-ignore : 'ARGS' found in JSON Spec
  class ImmediateStem<A extends Args[] | { [argName: string]: any } = [] | {}> implements ImmediateCommand {
    // @ts-ignore : 'Args' found in JSON Spec
    args: Args;
    stem: string;
    type: 'immediate';

    public static new<A extends any[] | { [argName: string]: any }>(opts: ImmediateOptions<A>): ImmediateStem<A>;

    // @ts-ignore : 'Command' found in JSON Spec
    public toSeqJson(): ImmediateCommand;

    // @ts-ignore : 'Metadata' found in JSON Spec
    public METADATA(metadata: Metadata): ImmediateStem<A>;
    // @ts-ignore : 'Metadata' found in JSON Spec
    public GET_METADATA(): Metadata | undefined;

    // @ts-ignore : 'Description' found in JSON Spec
    public DESCRIPTION(description: Description): ImmediateStem<A>;
    // @ts-ignore : 'Description' found in JSON Spec
    public GET_DESCRIPTION(): Description | undefined;
  }

  // @ts-ignore : 'HardwareCommand' found in JSON Spec
  class HardwareStem implements HardwareCommand {
    stem: string;
    type: 'hardware';

    public static new(opts: HardwareOptions): HardwareStem;

    // @ts-ignore : 'Command' found in JSON Spec
    public toSeqJson(): HardwareCommand;

    // @ts-ignore : 'Metadata' found in JSON Spec
    public METADATA(metadata: Metadata): HardwareStem;
    // @ts-ignore : 'Metadata' found in JSON Spec
    public GET_METADATA(): Metadata | undefined;

    // @ts-ignore : 'Description' found in JSON Spec
    public DESCRIPTION(description: Description): HardwareStem;
    // @ts-ignore : 'Description' found in JSON Spec
    public GET_DESCRIPTION(): Description | undefined;
  }

  const STEPS: {
    GROUND_BLOCK: typeof GROUND_BLOCK;
    GROUND_EVENT: typeof GROUND_EVENT;
  };

  type Context = {};
  type ExpansionReturn = Arrayable<CommandStem>;

  type U<BitLength extends 8 | 16 | 32 | 64> = number;
  type U8 = U<8>;
  type U16 = U<16>;
  type U32 = U<32>;
  type U64 = U<64>;
  type I<BitLength extends 8 | 16 | 32 | 64> = number;
  type I8 = I<8>;
  type I16 = I<16>;
  type I32 = I<32>;
  type I64 = I<64>;
  type VarString<PrefixBitLength extends number, MaxBitLength extends number> = string;
  type FixedString = string;
  type F<BitLength extends 32 | 64> = number;
  type F32 = F<32>;
  type F64 = F<64>;

  // @ts-ignore : 'Commands' found in generated code
  function A(...args: [TemplateStringsArray, ...string[]]): typeof Commands & typeof STEPS;
  // @ts-ignore : 'Commands' found in generated code
  function A(absoluteTime: Temporal.Instant): typeof Commands & typeof STEPS;
  // @ts-ignore : 'Commands' found in generated code
  function A(timeDOYString: string): typeof Commands & typeof STEPS;

  // @ts-ignore : 'Commands' found in generated code
  function R(...args: [TemplateStringsArray, ...string[]]): typeof Commands & typeof STEPS;
  // @ts-ignore : 'Commands' found in generated code
  function R(duration: Temporal.Duration): typeof Commands & typeof STEPS;
  // @ts-ignore : 'Commands' found in generated code
  function R(timeHMSString: string): typeof Commands & typeof STEPS;

  // @ts-ignore : 'Commands' found in generated code
  function E(...args: [TemplateStringsArray, ...string[]]): typeof Commands & typeof STEPS;
  // @ts-ignore : 'Commands' found in generated code
  function E(duration: Temporal.Duration): typeof Commands & typeof STEPS;
  // @ts-ignore : 'Commands' found in generated code
  function E(timeHMSString: string): typeof Commands & typeof STEPS;

  // @ts-ignore : 'Commands' found in generated code
  const C: typeof Commands & typeof STEPS;
}

/*
		  ---------------------------------
					  Sequence eDSL
		  ---------------------------------
		  */
// @ts-ignore : 'SeqJson' found in JSON Spec
export class Sequence implements SeqJson {
  public readonly id: string;
  // @ts-ignore : 'Metadata' found in JSON Spec
  public readonly metadata: Metadata;

  // @ts-ignore : 'VariableDeclaration' found in JSON Spec
  public readonly locals?: [VariableDeclaration, ...VariableDeclaration[]];
  // @ts-ignore : 'VariableDeclaration' found in JSON Spec
  public readonly parameters?: [VariableDeclaration, ...VariableDeclaration[]];
  // @ts-ignore : 'Step' found in JSON Spec
  public readonly steps?: Step[];
  // @ts-ignore : 'Request' found in JSON Spec
  public readonly requests?: Request[];
  // @ts-ignore : 'ImmediateCommand' found in JSON Spec
  public readonly immediate_commands?: ImmediateCommand[];
  // @ts-ignore : 'HardwareCommand' found in JSON Spec
  public readonly hardware_commands?: HardwareCommand[];
  [k: string]: unknown;

  // @ts-ignore : 'SeqJson' found in JSON Spec
  private constructor(opts: SequenceOptions | SeqJson) {
    if ('id' in opts) {
      this.id = opts.id;
    } else {
      this.id = opts.seqId;
    }
    this.metadata = opts.metadata;

    this.locals = opts.locals ?? undefined;
    this.parameters = opts.parameters ?? undefined;
    this.steps = opts.steps ?? undefined;
    this.requests = opts.requests ?? undefined;
    this.immediate_commands = opts.immediate_commands ?? undefined;
    this.hardware_commands = opts.hardware_commands ?? undefined;
  }
  public static new(opts: SequenceOptions): Sequence {
    return new Sequence(opts);
  }

  // @ts-ignore : 'SeqJson' found in JSON Spec
  public toSeqJson(): SeqJson {
    return {
      id: this.id,
      metadata: this.metadata,
      ...(this.steps
        ? {
            steps: this.steps.map(step => {
              if (step instanceof CommandStem || step instanceof Ground_Block || step instanceof Ground_Event)
                return step.toSeqJson();
              return step;
            }),
          }
        : {}),
      ...(this.locals ? { locals: this.locals } : {}),
      ...(this.parameters ? { parameters: this.parameters } : {}),
      ...(this.requests
        ? {
            requests: this.requests.map(request => {
              return {
                name: request.name,
                steps: [
                  request.steps[0] instanceof CommandStem ||
                  request.steps[0] instanceof Ground_Block ||
                  request.steps[0] instanceof Ground_Event
                    ? request.steps[0].toSeqJson()
                    : request.steps[0],
                  // @ts-ignore : 'step' found in JSON Spec
                  ...request.steps.slice(1).map(step => {
                    if (step instanceof CommandStem || step instanceof Ground_Block || step instanceof Ground_Event)
                      return step.toSeqJson();
                    return step;
                  }),
                ],
                type: request.type,
                ...(request.description ? { description: request.description } : {}),
                ...(request.ground_epoch ? { ground_epoch: request.ground_epoch } : {}),
                ...(request.time ? { time: request.time } : {}),
                ...(request.metadata ? { metadata: request.metadata } : {}),
              };
            }),
          }
        : {}),
      ...(this.immediate_commands
        ? {
            immediate_commands: this.immediate_commands.map(command => {
              if (command instanceof ImmediateStem) return command.toSeqJson();
              else return command;
            }),
          }
        : {}),
      ...(this.hardware_commands
        ? {
            hardware_commands: this.hardware_commands.map(h => {
              return h instanceof HardwareStem ? h.toSeqJson() : h;
            }),
          }
        : {}),
    };
  }

  public toEDSLString(): string {
    const commandsString =
      this.steps && this.steps.length > 0
        ? '[\n' +
          indent(
            this.steps
              .map(step => {
                if (step instanceof CommandStem || step instanceof Ground_Block || step instanceof Ground_Event) {
                  return step.toEDSLString() + ',';
                }
                return objectToString(step) + ',';
              })
              .join('\n'),
            1,
          ) +
          '\n]'
        : '';
    //ex.
    // [C.ADD_WATER]
    const metadataString = Object.keys(this.metadata).length == 0 ? `{}` : `${objectToString(this.metadata)}`;

    const localsString = this.locals ? `[\n${indent(this.locals.map(l => objectToString(l)).join(',\n'), 1)}\n]` : '';

    const parameterString = this.parameters
      ? `[\n${indent(this.parameters.map(l => objectToString(l)).join(',\n'), 1)}\n]`
      : '';
    //ex.
    // `parameters: [
    //   {
    //     allowable_ranges: [
    //       {
    //         max: 3600,
    //         min: 1,
    //       },
    //     ],
    //     name: 'duration',
    //     type: 'UINT',
    //   }
    // ]`;

    const hardwareString = this.hardware_commands
      ? `[\n${indent(this.hardware_commands.map(h => (h as HardwareStem).toEDSLString()).join(',\n'), 1)}\n]`
      : '';
    //ex.
    // hardware_commands: [
    //   {
    //     description: 'FIRE THE PYROS',
    //     metadata:{
    //       author: 'rrgoetz',
    //     },
    //     stem: 'HDW_PYRO_ENGINE',
    //   }
    // ],

    const immediateString =
      this.immediate_commands && this.immediate_commands.length > 0
        ? '[\n' +
          indent(
            this.immediate_commands
              .map(command => {
                if (command instanceof ImmediateStem) {
                  return command.toEDSLString() + ',';
                }
                return objectToString(command) + ',';
              })
              .join('\n'),
            1,
          ) +
          '\n]'
        : '';
    //ex.
    // immediate_commands: [
    //   {
    //     args: [
    //       {
    //         name: 'direction',
    //         type: 'string',
    //         value: 'FromStem',
    //       },
    //     ],
    //     stem: 'PEEL_BANANA',
    //   }
    // ]

    const requestString = this.requests
      ? `[\n${indent(
          this.requests
            .map(r => {
              return (
                `{\n` +
                indent(
                  `name: '${r.name}',\n` +
                    `steps: [\n${indent(
                      r.steps
                        // @ts-ignore : 's: Step' found in JSON Spec
                        .map(s => {
                          if (s instanceof CommandStem || s instanceof Ground_Block || s instanceof Ground_Event) {
                            return s.toEDSLString() + ',';
                          }
                          return objectToString(s) + ',';
                        })
                        .join('\n'),
                      1,
                    )}\n],` +
                    `\ntype: '${r.type}',` +
                    `${r.description ? `\ndescription: '${r.description}',` : ''}` +
                    `${r.ground_epoch ? `\nground_epoch: ${objectToString(r.ground_epoch)},` : ''}` +
                    `${r.time ? `\ntime: ${objectToString(r.time)},` : ''}` +
                    `${r.metadata ? `\nmetadata: ${objectToString(r.metadata)},` : ''}`,
                  1,
                ) +
                `\n}`
              );
            })
            .join(',\n'),
          1,
        )}\n]`
      : '';
    //ex.
    /*requests: [
    {
      name: 'power',
      steps: [
      R`04:39:22.000`.PREHEAT_OVEN({
      temperature: 360,
      }),
      C.ADD_WATER,
      ],
      type: 'request',
      description: ' Activate the oven',
      ground_epoch: {
      delta: 'now',
      name: 'activate',
      },
      metadata: {
      author: 'rrgoet',
      },
    }
    ]
    }*/

    return (
      `export default () =>\n` +
      `${indent(`Sequence.new({`, 1)}\n` +
      `${indent(`seqId: '${this.id}'`, 2)},\n` +
      `${indent(`metadata: ${metadataString}`, 2)},\n` +
      `${localsString.length !== 0 ? `${indent(`locals: ${localsString}`, 2)},\n` : ''}` +
      `${parameterString.length !== 0 ? `${indent(`parameters: ${parameterString}`, 2)},\n` : ''}` +
      `${commandsString.length !== 0 ? `${indent(`steps: ${commandsString}`, 2)},\n` : ''}` +
      `${hardwareString.length !== 0 ? `${indent(`hardware_commands: ${hardwareString}`, 2)},\n` : ''}` +
      `${immediateString.length !== 0 ? `${indent(`immediate_commands: ${immediateString}`, 2)},\n` : ''}` +
      `${requestString.length !== 0 ? `${indent(`requests: ${requestString}`, 2)},\n` : ''}` +
      `${indent(`});`, 1)}`
    );
  }

  // @ts-ignore : 'Args' found in JSON Spec
  public static fromSeqJson(json: SeqJson): Sequence {
    return Sequence.new({
      seqId: json.id,
      metadata: json.metadata,
      // @ts-ignore : 'Step' found in JSON Spec
      ...(json.steps
        ? {
            // @ts-ignore : 'Step' found in JSON Spec
            steps: json.steps.map((c: Step) => {
              if (c.type === 'command') return CommandStem.fromSeqJson(c as CommandStem);
              else if (c.type === 'ground_block') return Ground_Block.fromSeqJson(c as Ground_Block);
              else if (c.type === 'ground_event') return Ground_Event.fromSeqJson(c as Ground_Event);
              return c;
            }),
          }
        : {}),
      ...(json.locals ? { locals: json.locals } : {}),
      ...(json.parameters ? { parameters: json.parameters } : {}),
      ...(json.requests
        ? {
            // @ts-ignore : 'r: Request' found in JSON Spec
            requests: json.requests.map(r => {
              return {
                name: r.name,
                type: r.type,
                ...(r.description ? { description: r.description } : {}),
                ...(r.ground_epoch ? { ground_epoch: r.ground_epoch } : {}),
                ...(r.time ? { time: r.time } : {}),
                ...(r.metadata ? { metadata: r.metadata } : {}),
                steps: [
                  r.steps[0].type === 'command'
                    ? CommandStem.fromSeqJson(r.steps[0] as CommandStem)
                    : r.steps[0].type === 'ground_block'
                    ? // @ts-ignore : 'GroundBlock' found in JSON Spec
                      Ground_Block.fromSeqJson(r.steps[0] as GroundBlock)
                    : r.steps[0].type === 'ground_event'
                    ? // @ts-ignore : 'GroundEvent' found in JSON Spec
                      Ground_Event.fromSeqJson(r.steps[0] as GroundEvent)
                    : r.steps[0],
                  // @ts-ignore : 'step : Step' found in JSON Spec
                  ...r.steps.slice(1).map(step => {
                    if (step.type === 'command') return CommandStem.fromSeqJson(step as CommandStem);
                    else if (step.type === 'ground_block') return Ground_Block.fromSeqJson(step as Ground_Block);
                    else if (step.type === 'ground_event') return Ground_Event.fromSeqJson(step as Ground_Event);
                    return step;
                  }),
                ],
              };
            }),
          }
        : {}),
      ...(json.immediate_commands
        ? {
            // @ts-ignore : 'Step' found in JSON Spec
            immediate_commands: json.immediate_commands.map((c: ImmediateCommand) => ImmediateStem.fromSeqJson(c)),
          }
        : {}),
      ...(json.hardware_commands
        ? // @ts-ignore : 'HardwareCommand' found in JSON Spec
          { hardware_commands: json.hardware_commands.map((h: HardwareCommand) => HardwareStem.fromSeqJson(h)) }
        : {}),
    });
  }
}

/*
	  ---------------------------------
				  STEPS eDSL
	  ---------------------------------
	  */

// @ts-ignore : 'Args' found in JSON Spec
export class CommandStem<A extends Args[] | { [argName: string]: any } = [] | {}> implements Command {
  public readonly arguments: A;
  public readonly absoluteTime: Temporal.Instant | null = null;
  public readonly epochTime: Temporal.Duration | null = null;
  public readonly relativeTime: Temporal.Duration | null = null;

  public readonly stem: string;
  // @ts-ignore : 'Args' found in JSON Spec
  public readonly args!: Args;
  // @ts-ignore : 'Time' found in JSON Spec
  public readonly time!: Time;
  // @ts-ignore : 'Model' found in JSON Spec
  private readonly _models?: Model[] | undefined;
  // @ts-ignore : 'Metadata' found in JSON Spec
  private readonly _metadata?: Metadata | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  private readonly _description?: Description | undefined;
  public readonly type: 'command' = 'command';

  private constructor(opts: CommandOptions<A>) {
    this.stem = opts.stem;
    this.arguments = opts.arguments;

    if ('absoluteTime' in opts) {
      this.absoluteTime = opts.absoluteTime;
    } else if ('epochTime' in opts) {
      this.epochTime = opts.epochTime;
    } else if ('relativeTime' in opts) {
      this.relativeTime = opts.relativeTime;
    }
    this._metadata = opts.metadata;
    this._description = opts.description;
    this._models = opts.models;
  }

  public static new<A extends any[] | { [argName: string]: any }>(opts: CommandOptions<A>): CommandStem<A> {
    if ('absoluteTime' in opts) {
      return new CommandStem<A>({
        ...opts,
        absoluteTime: opts.absoluteTime,
      });
    } else if ('epochTime' in opts) {
      return new CommandStem<A>({
        ...opts,
        epochTime: opts.epochTime,
      });
    } else if ('relativeTime' in opts) {
      return new CommandStem<A>({
        ...opts,
        relativeTime: opts.relativeTime,
      });
    } else {
      return new CommandStem<A>(opts);
    }
  }

  // @ts-ignore : 'Model' found in JSON Spec
  public MODELS(models: Model[]): CommandStem {
    return CommandStem.new({
      stem: this.stem,
      arguments: this.arguments,
      models: models,
      metadata: this._metadata,
      description: this._description,
      ...(this.absoluteTime && { absoluteTime: this.absoluteTime }),
      ...(this.epochTime && { epochTime: this.epochTime }),
      ...(this.relativeTime && { relativeTime: this.relativeTime }),
    });
  }

  // @ts-ignore : 'Model' found in JSON Spec
  public GET_MODELS(): Model[] | undefined {
    return this._models;
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public METADATA(metadata: Metadata): CommandStem {
    return CommandStem.new({
      stem: this.stem,
      arguments: this.arguments,
      models: this._models,
      metadata: metadata,
      description: this._description,
      ...(this.absoluteTime && { absoluteTime: this.absoluteTime }),
      ...(this.epochTime && { epochTime: this.epochTime }),
      ...(this.relativeTime && { relativeTime: this.relativeTime }),
    });
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public GET_METADATA(): Metadata | undefined {
    return this._metadata;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public DESCRIPTION(description: Description): CommandStem {
    return CommandStem.new({
      stem: this.stem,
      arguments: this.arguments,
      models: this._models,
      metadata: this._metadata,
      description: description,
      ...(this.absoluteTime && { absoluteTime: this.absoluteTime }),
      ...(this.epochTime && { epochTime: this.epochTime }),
      ...(this.relativeTime && { relativeTime: this.relativeTime }),
    });
  }
  // @ts-ignore : 'Description' found in JSON Spec
  public GET_DESCRIPTION(): Description | undefined {
    return this._description;
  }

  // @ts-ignore : 'Command' found in JSON Spec
  public toSeqJson(): Command {
    return {
      args: convertArgsToInterfaces(this.arguments),
      stem: this.stem,
      time:
        this.absoluteTime !== null
          ? { type: TimingTypes.ABSOLUTE, tag: instantToDoy(this.absoluteTime) }
          : this.epochTime !== null
          ? { type: TimingTypes.EPOCH_RELATIVE, tag: durationToHms(this.epochTime) }
          : this.relativeTime !== null
          ? { type: TimingTypes.COMMAND_RELATIVE, tag: durationToHms(this.relativeTime) }
          : { type: TimingTypes.COMMAND_COMPLETE },
      type: this.type,
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { models: this._models } : {}),
      ...(this._description ? { description: this._description } : {}),
    };
  }

  // @ts-ignore : 'Command' found in JSON Spec
  public static fromSeqJson(json: Command): CommandStem {
    const timeValue =
      json.time.type === TimingTypes.ABSOLUTE
        ? { absoluteTime: doyToInstant(json.time.tag as DOY_STRING) }
        : json.time.type === TimingTypes.COMMAND_RELATIVE
        ? { relativeTime: hmsToDuration(json.time.tag as HMS_STRING) }
        : json.time.type === TimingTypes.EPOCH_RELATIVE
        ? { epochTime: hmsToDuration(json.time.tag as HMS_STRING) }
        : {};

    return CommandStem.new({
      stem: json.stem,
      arguments: convertInterfacesToArgs(json.args),
      metadata: json.metadata,
      models: json.models,
      description: json.description,
      ...timeValue,
    });
  }

  public absoluteTiming(absoluteTime: Temporal.Instant): CommandStem<A> {
    return CommandStem.new({
      stem: this.stem,
      arguments: this.arguments,
      absoluteTime: absoluteTime,
    });
  }

  public epochTiming(epochTime: Temporal.Duration): CommandStem<A> {
    return CommandStem.new({
      stem: this.stem,
      arguments: this.arguments,
      epochTime: epochTime,
    });
  }

  public relativeTiming(relativeTime: Temporal.Duration): CommandStem<A> {
    return CommandStem.new({
      stem: this.stem,
      arguments: this.arguments,
      relativeTime: relativeTime,
    });
  }

  public toEDSLString(): string {
    const timeString = this.absoluteTime
      ? `A\`${instantToDoy(this.absoluteTime)}\``
      : this.epochTime
      ? `E\`${durationToHms(this.epochTime)}\``
      : this.relativeTime
      ? `R\`${durationToHms(this.relativeTime)}\``
      : 'C';

    const argsString = Object.keys(this.arguments).length === 0 ? '' : `(${argumentsToString(this.arguments)})`;

    const metadata =
      this._metadata && Object.keys(this._metadata).length !== 0
        ? `\n.METADATA(${objectToString(this._metadata)})`
        : '';
    const description =
      this._description && this._description.length !== 0 ? `\n.DESCRIPTION('${this._description}')` : '';
    const models =
      this._models && Object.keys(this._models).length !== 0
        ? `\n.MODELS([\n${this._models.map(m => indent(objectToString(m))).join(',\n')}\n])`
        : '';
    return `${timeString}.${this.stem}${argsString}${description}${metadata}${models}`;
  }
}

// @ts-ignore : 'Args' found in JSON Spec
export class ImmediateStem<A extends Args[] | { [argName: string]: any } = [] | {}> implements ImmediateCommand {
  public readonly arguments: A;
  public readonly stem: string;
  // @ts-ignore : 'Args' found in JSON Spec
  public readonly args!: Args;
  // @ts-ignore : 'Metadata' found in JSON Spec
  private readonly _metadata?: Metadata | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  private readonly _description?: Description | undefined;
  public readonly type: 'immediate' = 'immediate';

  private constructor(opts: ImmediateOptions<A>) {
    this.stem = opts.stem;
    this.arguments = opts.arguments;
    this._metadata = opts.metadata;
    this._description = opts.description;
  }

  public static new<A extends any[] | { [argName: string]: any }>(opts: ImmediateOptions<A>): ImmediateStem<A> {
    return new ImmediateStem<A>(opts);
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public METADATA(metadata: Metadata): ImmediateStem {
    return ImmediateStem.new({
      stem: this.stem,
      arguments: this.arguments,
      metadata: metadata,
      description: this._description,
    });
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public GET_METADATA(): Metadata | undefined {
    return this._metadata;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public DESCRIPTION(description: Description): ImmediateStem {
    return ImmediateStem.new({
      stem: this.stem,
      arguments: this.arguments,
      metadata: this._metadata,
      description: description,
    });
  }
  // @ts-ignore : 'Description' found in JSON Spec
  public GET_DESCRIPTION(): Description | undefined {
    return this._description;
  }

  // @ts-ignore : 'Command' found in JSON Spec
  public toSeqJson(): ImmediateCommand {
    return {
      args: convertArgsToInterfaces(this.arguments),
      stem: this.stem,
      type: this.type,
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._description ? { description: this._description } : {}),
    };
  }

  // @ts-ignore : 'Command' found in JSON Spec
  public static fromSeqJson(json: ImmediateCommand): ImmediateStem {
    return ImmediateStem.new({
      stem: json.stem,
      arguments: convertInterfacesToArgs(json.args),
      metadata: json.metadata,
      description: json.description,
    });
  }

  public toEDSLString(): string {
    const argsString = Object.keys(this.arguments).length === 0 ? '' : `(${argumentsToString(this.arguments)})`;

    const metadata =
      this._metadata && Object.keys(this._metadata).length !== 0
        ? `\n.METADATA(${objectToString(this._metadata)})`
        : '';
    const description =
      this._description && this._description.length !== 0 ? `\n.DESCRIPTION('${this._description}')` : '';

    return `${this.stem}${argsString}${description}${metadata}`;
  }
}

//The function takes an object of arguments and converts them into the Args type. It does this by looping through the
// values and pushing a new argument type to the result array depending on the type of the value.
// If the value is an array, it will create a RepeatArgument type and recursively call on the values of the array.
// the function returns the result array of argument types -
// StringArgument, NumberArgument, BooleanArgument, SymbolArgument, HexArgument, and RepeatArgument.
// @ts-ignore : 'Args' found in JSON Spec
function convertArgsToInterfaces(args: { [argName: string]: any }): Args {
  // @ts-ignore : 'Args' found in JSON Spec
  let result: Args = [];
  if (args['length'] === 0) {
    return result;
  }

  const values = Array.isArray(args) ? args[0] : args;

  for (let key in values) {
    let value = values[key];
    if (Array.isArray(value)) {
      // @ts-ignore : 'RepeatArgument' found in JSON Spec
      let repeatArg: RepeatArgument = {
        value: value.map(arg => {
          return convertRepeatArgs(arg);
        }),
        type: 'repeat',
        name: key,
      };
      result.push(repeatArg);
    } else {
      result = result.concat(convertValueToObject(value, key));
    }
  }
  return result;
}

// @ts-ignore : 'GroundBlock' found in JSON Spec
export class Ground_Block implements GroundBlock {
  name: string;
  // @ts-ignore : 'Time' found in JSON Spec
  time!: Time;
  type: 'ground_block' = 'ground_block';

  private readonly _absoluteTime: Temporal.Instant | null = null;
  private readonly _epochTime: Temporal.Duration | null = null;
  private readonly _relativeTime: Temporal.Duration | null = null;

  // @ts-ignore : 'Args' found in JSON Spec
  private readonly _args: Args | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  private readonly _description: Description | undefined;
  // @ts-ignore : 'Metadata' found in JSON Spec
  private readonly _metadata: Metadata | undefined;
  // @ts-ignore : 'Model' found in JSON Spec
  private readonly _models: Model[] | undefined;

  constructor(opts: GroundOptions) {
    this.name = opts.name;

    this._args = opts.args ?? undefined;
    this._description = opts.description ?? undefined;
    this._metadata = opts.metadata ?? undefined;
    this._models = opts.models ?? undefined;

    if ('absoluteTime' in opts) {
      this._absoluteTime = opts.absoluteTime;
    } else if ('epochTime' in opts) {
      this._epochTime = opts.epochTime;
    } else if ('relativeTime' in opts) {
      this._relativeTime = opts.relativeTime;
    }
  }

  public static new(opts: GroundOptions): Ground_Block {
    return new Ground_Block(opts);
  }

  public absoluteTiming(absoluteTime: Temporal.Instant): Ground_Block {
    return new Ground_Block({
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { model: this._models } : {}),
      name: this.name,
      absoluteTime: absoluteTime,
    });
  }

  public epochTiming(epochTime: Temporal.Duration): Ground_Block {
    return new Ground_Block({
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { model: this._models } : {}),
      name: this.name,
      epochTime: epochTime,
    });
  }

  public relativeTiming(relativeTime: Temporal.Duration): Ground_Block {
    return new Ground_Block({
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { model: this._models } : {}),
      name: this.name,
      relativeTime: relativeTime,
    });
  }

  // @ts-ignore : 'Model' found in JSON Spec
  public MODELS(models: Model[]): Ground_Block {
    return Ground_Block.new({
      name: this.name,
      models: models,
      ...(this._args && { args: this._args }),
      ...(this._description && { description: this._description }),
      ...(this._metadata && { metadata: this._metadata }),
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }

  // @ts-ignore : 'Model' found in JSON Spec
  public GET_MODELS(): Model[] | undefined {
    return this._models;
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public METADATA(metadata: Metadata): Ground_Block {
    return Ground_Block.new({
      name: this.name,
      ...(this._models && { models: this._models }),
      ...(this._args && { args: this._args }),
      ...(this._description && { description: this._description }),
      metadata: metadata,
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public GET_METADATA(): Metadata | undefined {
    return this._metadata;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public DESCRIPTION(description: Description): Ground_Block {
    return Ground_Block.new({
      name: this.name,
      ...(this._models && { models: this._models }),
      ...(this._args && { args: this._args }),
      description: description,
      ...(this._metadata && { metadata: this._metadata }),
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }
  // @ts-ignore : 'Description' found in JSON Spec
  public GET_DESCRIPTION(): Description | undefined {
    return this._description;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public ARGUMENTS(args: Args): Ground_Block {
    return Ground_Block.new({
      name: this.name,
      ...(this._models && { models: this._models }),
      args: args,
      ...(this._description && { description: this._description }),
      ...(this._metadata && { metadata: this._metadata }),
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public GET_ARGUMENTS(): Args | undefined {
    return this._args;
  }

  // @ts-ignore : 'GroundBlock' found in JSON Spec
  public toSeqJson(): GroundBlock {
    return {
      name: this.name,
      time:
        this._absoluteTime !== null
          ? { type: TimingTypes.ABSOLUTE, tag: instantToDoy(this._absoluteTime) }
          : this._epochTime !== null
          ? { type: TimingTypes.EPOCH_RELATIVE, tag: durationToHms(this._epochTime) }
          : this._relativeTime !== null
          ? { type: TimingTypes.COMMAND_RELATIVE, tag: durationToHms(this._relativeTime) }
          : { type: TimingTypes.COMMAND_COMPLETE },
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { models: this._models } : {}),
      type: this.type,
    };
  }

  // @ts-ignore : 'GroundBlock' found in JSON Spec
  public static fromSeqJson(json: GroundBlock): Ground_Block {
    const timeValue =
      json.time.type === TimingTypes.ABSOLUTE
        ? { absoluteTime: doyToInstant(json.time.tag as DOY_STRING) }
        : json.time.type === TimingTypes.COMMAND_RELATIVE
        ? { relativeTime: hmsToDuration(json.time.tag as HMS_STRING) }
        : json.time.type === TimingTypes.EPOCH_RELATIVE
        ? { epochTime: hmsToDuration(json.time.tag as HMS_STRING) }
        : {};

    return Ground_Block.new({
      name: json.name,
      ...(json.args ? { args: json.args } : {}),
      ...(json.description ? { description: json.description } : {}),
      ...(json.metadata ? { metadata: json.metadata } : {}),
      ...(json.models ? { models: json.models } : {}),
      ...timeValue,
    });
  }

  public toEDSLString(): string {
    const timeString = this._absoluteTime
      ? `A\`${instantToDoy(this._absoluteTime)}\``
      : this._epochTime
      ? `E\`${durationToHms(this._epochTime)}\``
      : this._relativeTime
      ? `R\`${durationToHms(this._relativeTime)}\``
      : 'C';

    const args =
      this._args && Object.keys(this._args).length !== 0
        ? // @ts-ignore : 'A : Args' found in JSON Spec
          `\n.ARGUMENTS([\n${this._args.map(a => indent(objectToString(a))).join(',\n')}\n])`
        : '';

    const metadata =
      this._metadata && Object.keys(this._metadata).length !== 0
        ? `\n.METADATA(${objectToString(this._metadata)})`
        : '';

    const description =
      this._description && this._description.length !== 0 ? `\n.DESCRIPTION('${this._description}')` : '';

    const models =
      this._models && Object.keys(this._models).length !== 0
        ? `\n.MODELS([\n${this._models.map(m => indent(objectToString(m))).join(',\n')}\n])`
        : '';

    return `${timeString}.GROUND_BLOCK('${this.name}')${args}${description}${metadata}${models}`;
  }
}

/**
 * This is a Ground Block step
 *
 */
function GROUND_BLOCK(name: string) {
  return new Ground_Block({ name: name });
}

// @ts-ignore : 'GroundBlock' found in JSON Spec
export class Ground_Event implements GroundEvent {
  name: string;
  // @ts-ignore : 'Time' found in JSON Spec
  time!: Time;
  type: 'ground_event' = 'ground_event';

  private readonly _absoluteTime: Temporal.Instant | null = null;
  private readonly _epochTime: Temporal.Duration | null = null;
  private readonly _relativeTime: Temporal.Duration | null = null;

  // @ts-ignore : 'Args' found in JSON Spec
  private readonly _args: Args | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  private readonly _description: Description | undefined;
  // @ts-ignore : 'Metadata' found in JSON Spec
  private readonly _metadata: Metadata | undefined;
  // @ts-ignore : 'Model' found in JSON Spec
  private readonly _models: Model[] | undefined;

  constructor(opts: GroundOptions) {
    this.name = opts.name;

    this._args = opts.args ?? undefined;
    this._description = opts.description ?? undefined;
    this._metadata = opts.metadata ?? undefined;
    this._models = opts.models ?? undefined;

    if ('absoluteTime' in opts) {
      this._absoluteTime = opts.absoluteTime;
    } else if ('epochTime' in opts) {
      this._epochTime = opts.epochTime;
    } else if ('relativeTime' in opts) {
      this._relativeTime = opts.relativeTime;
    }
  }

  public static new(opts: GroundOptions): Ground_Event {
    return new Ground_Event(opts);
  }

  public absoluteTiming(absoluteTime: Temporal.Instant): Ground_Event {
    return new Ground_Event({
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { model: this._models } : {}),
      name: this.name,
      absoluteTime: absoluteTime,
    });
  }

  public epochTiming(epochTime: Temporal.Duration): Ground_Event {
    return new Ground_Event({
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { model: this._models } : {}),
      name: this.name,
      epochTime: epochTime,
    });
  }

  public relativeTiming(relativeTime: Temporal.Duration): Ground_Event {
    return new Ground_Event({
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { model: this._models } : {}),
      name: this.name,
      relativeTime: relativeTime,
    });
  }

  // @ts-ignore : 'Model' found in JSON Spec
  public MODELS(models: Model[]): Ground_Event {
    return Ground_Event.new({
      name: this.name,
      models: models,
      ...(this._args && { args: this._args }),
      ...(this._description && { description: this._description }),
      ...(this._metadata && { metadata: this._metadata }),
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }

  // @ts-ignore : 'Model' found in JSON Spec
  public GET_MODELS(): Model[] | undefined {
    return this._models;
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public METADATA(metadata: Metadata): Ground_Event {
    return Ground_Event.new({
      name: this.name,
      ...(this._models && { models: this._models }),
      ...(this._args && { args: this._args }),
      ...(this._description && { description: this._description }),
      metadata: metadata,
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public GET_METADATA(): Metadata | undefined {
    return this._metadata;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public DESCRIPTION(description: Description): Ground_Event {
    return Ground_Event.new({
      name: this.name,
      ...(this._models && { models: this._models }),
      ...(this._args && { args: this._args }),
      description: description,
      ...(this._metadata && { metadata: this._metadata }),
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }
  // @ts-ignore : 'Description' found in JSON Spec
  public GET_DESCRIPTION(): Description | undefined {
    return this._description;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public ARGUMENTS(args: Args): Ground_Event {
    return Ground_Event.new({
      name: this.name,
      ...(this._models && { models: this._models }),
      args: args,
      ...(this._description && { description: this._description }),
      ...(this._metadata && { metadata: this._metadata }),
      ...(this._absoluteTime && { absoluteTime: this._absoluteTime }),
      ...(this._epochTime && { epochTime: this._epochTime }),
      ...(this._relativeTime && { relativeTime: this._relativeTime }),
    });
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public GET_ARGUMENTS(): Args | undefined {
    return this._args;
  }

  // @ts-ignore : 'Ground_Event' found in JSON Spec
  public toSeqJson(): GroundEvent {
    return {
      name: this.name,
      time:
        this._absoluteTime !== null
          ? { type: TimingTypes.ABSOLUTE, tag: instantToDoy(this._absoluteTime) }
          : this._epochTime !== null
          ? { type: TimingTypes.EPOCH_RELATIVE, tag: durationToHms(this._epochTime) }
          : this._relativeTime !== null
          ? { type: TimingTypes.COMMAND_RELATIVE, tag: durationToHms(this._relativeTime) }
          : { type: TimingTypes.COMMAND_COMPLETE },
      ...(this._args ? { args: this._args } : {}),
      ...(this._description ? { description: this._description } : {}),
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._models ? { models: this._models } : {}),
      type: this.type,
    };
  }

  // @ts-ignore : 'GroundEvent' found in JSON Spec
  public static fromSeqJson(json: GroundEvent): Ground_Event {
    const timeValue =
      json.time.type === TimingTypes.ABSOLUTE
        ? { absoluteTime: doyToInstant(json.time.tag as DOY_STRING) }
        : json.time.type === TimingTypes.COMMAND_RELATIVE
        ? { relativeTime: hmsToDuration(json.time.tag as HMS_STRING) }
        : json.time.type === TimingTypes.EPOCH_RELATIVE
        ? { epochTime: hmsToDuration(json.time.tag as HMS_STRING) }
        : {};

    return Ground_Event.new({
      name: json.name,
      ...(json.args ? { args: json.args } : {}),
      ...(json.description ? { description: json.description } : {}),
      ...(json.metadata ? { metadata: json.metadata } : {}),
      ...(json.models ? { models: json.models } : {}),
      ...timeValue,
    });
  }

  public toEDSLString(): string {
    const timeString = this._absoluteTime
      ? `A\`${instantToDoy(this._absoluteTime)}\``
      : this._epochTime
      ? `E\`${durationToHms(this._epochTime)}\``
      : this._relativeTime
      ? `R\`${durationToHms(this._relativeTime)}\``
      : 'C';

    const args =
      this._args && Object.keys(this._args).length !== 0
        ? // @ts-ignore : 'A : Args' found in JSON Spec
          `\n.ARGUMENTS([\n${this._args.map(a => indent(objectToString(a))).join(',\n')}\n])`
        : '';

    const metadata =
      this._metadata && Object.keys(this._metadata).length !== 0
        ? `\n.METADATA(${objectToString(this._metadata)})`
        : '';

    const description =
      this._description && this._description.length !== 0 ? `\n.DESCRIPTION('${this._description}')` : '';

    const models =
      this._models && Object.keys(this._models).length !== 0
        ? `\n.MODELS([\n${this._models.map(m => indent(objectToString(m))).join(',\n')}\n])`
        : '';

    return `${timeString}.GROUND_EVENT('${this.name}')${args}${description}${metadata}${models}`;
  }
}

/**
 * This is a Ground Event step
 *
 */
function GROUND_EVENT(name: string) {
  return new Ground_Event({ name: name });
}

export const STEPS = {
  GROUND_BLOCK: GROUND_BLOCK,
  GROUND_EVENT: GROUND_EVENT,
};

/*-----------------------------------
		HW Commands
	  ------------------------------------- */
// @ts-ignore : 'HardwareCommand' found in JSON Spec
export class HardwareStem implements HardwareCommand {
  public readonly stem: string;
  // @ts-ignore : 'Metadata' found in JSON Spec
  private readonly _metadata?: Metadata | undefined;
  // @ts-ignore : 'Description' found in JSON Spec
  private readonly _description?: Description | undefined;
  public readonly type: 'hardware' = 'hardware';

  private constructor(opts: HardwareOptions) {
    this.stem = opts.stem;
    this._metadata = opts.metadata;
    this._description = opts.description;
  }

  public static new(opts: HardwareOptions): HardwareStem {
    return new HardwareStem(opts);
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public METADATA(metadata: Metadata): HardwareStem {
    return HardwareStem.new({
      stem: this.stem,
      metadata: metadata,
      description: this._description,
    });
  }

  // @ts-ignore : 'Metadata' found in JSON Spec
  public GET_METADATA(): Metadata | undefined {
    return this._metadata;
  }

  // @ts-ignore : 'Description' found in JSON Spec
  public DESCRIPTION(description: Description): HardwareStem {
    return HardwareStem.new({
      stem: this.stem,
      metadata: this._metadata,
      description: description,
    });
  }
  // @ts-ignore : 'Description' found in JSON Spec
  public GET_DESCRIPTION(): Description | undefined {
    return this._description;
  }

  // @ts-ignore : 'Command' found in JSON Spec
  public toSeqJson(): HardwareCommand {
    return {
      stem: this.stem,
      type: this.type,
      ...(this._metadata ? { metadata: this._metadata } : {}),
      ...(this._description ? { description: this._description } : {}),
    };
  }

  // @ts-ignore : 'Command' found in JSON Spec
  public static fromSeqJson(json: HardwareCommand): HardwareStem {
    return HardwareStem.new({
      stem: json.stem,
      metadata: json.metadata,
      description: json.description,
    });
  }

  public toEDSLString(): string {
    const metadata =
      this._metadata && Object.keys(this._metadata).length !== 0
        ? `\n.METADATA(${objectToString(this._metadata)})`
        : '';
    const description =
      this._description && this._description.length !== 0 ? `\n.DESCRIPTION('${this._description}')` : '';

    return `${this.stem}${description}${metadata}`;
  }
}

/*
	  ---------------------------------
			  Time Utilities
	  ---------------------------------
	  */

export type DOY_STRING = string & { __brand: 'DOY_STRING' };
export type HMS_STRING = string & { __brand: 'HMS_STRING' };

const DOY_REGEX = /(\d{4})-(\d{3})T(\d{2}):(\d{2}):(\d{2})(?:\.(\d{3}))?/;
const HMS_REGEX = /(\d{2}):(\d{2}):(\d{2})(?:\.(\d{3}))?/;

/** YYYY-DOYTHH:MM:SS.sss */
export function instantToDoy(time: Temporal.Instant): DOY_STRING {
  const utcZonedDate = time.toZonedDateTimeISO('UTC');
  const YYYY = formatNumber(utcZonedDate.year, 4);
  const DOY = formatNumber(utcZonedDate.dayOfYear, 3);
  const HH = formatNumber(utcZonedDate.hour, 2);
  const MM = formatNumber(utcZonedDate.minute, 2);
  const SS = formatNumber(utcZonedDate.second, 2);
  const sss = formatNumber(utcZonedDate.millisecond, 3);
  return `${YYYY}-${DOY}T${HH}:${MM}:${SS}.${sss}` as DOY_STRING;
}

export function doyToInstant(doy: DOY_STRING): Temporal.Instant {
  const match = doy.match(DOY_REGEX);
  if (match === null) {
    throw new Error(`Invalid DOY string: ${doy}`);
  }
  const [, year, doyStr, hour, minute, second, millisecond] = match as [
    unknown,
    string,
    string,
    string,
    string,
    string,
    string | undefined,
  ];

  //use to convert doy to month and day
  const doyDate = new Date(parseInt(year, 10), 0, parseInt(doyStr, 10));
  // convert to UTC Date
  const utcDoyDate = new Date(
    Date.UTC(
      doyDate.getUTCFullYear(),
      doyDate.getUTCMonth(),
      doyDate.getUTCDate(),
      doyDate.getUTCHours(),
      doyDate.getUTCMinutes(),
      doyDate.getUTCSeconds(),
      doyDate.getUTCMilliseconds(),
    ),
  );

  return Temporal.ZonedDateTime.from({
    year: parseInt(year, 10),
    month: utcDoyDate.getUTCMonth() + 1,
    day: utcDoyDate.getUTCDate(),
    hour: parseInt(hour, 10),
    minute: parseInt(minute, 10),
    second: parseInt(second, 10),
    millisecond: parseInt(millisecond ?? '0', 10),
    timeZone: 'UTC',
  }).toInstant();
}

/** HH:MM:SS.sss */
export function durationToHms(time: Temporal.Duration): HMS_STRING {
  const HH = formatNumber(time.hours, 2);
  const MM = formatNumber(time.minutes, 2);
  const SS = formatNumber(time.seconds, 2);
  const sss = formatNumber(time.milliseconds, 3);

  return `${HH}:${MM}:${SS}.${sss}` as HMS_STRING;
}

export function hmsToDuration(hms: HMS_STRING): Temporal.Duration {
  const match = hms.match(HMS_REGEX);
  if (match === null) {
    throw new Error(`Invalid HMS string: ${hms}`);
  }
  const [, hours, minutes, seconds, milliseconds] = match as [unknown, string, string, string, string | undefined];
  return Temporal.Duration.from({
    hours: parseInt(hours, 10),
    minutes: parseInt(minutes, 10),
    seconds: parseInt(seconds, 10),
    milliseconds: parseInt(milliseconds ?? '0', 10),
  });
}

function formatNumber(number: number, size: number): string {
  return number.toString().padStart(size, '0');
}

// @ts-ignore : Used in generated code
function A(
  ...args: [TemplateStringsArray, ...string[]] | [Temporal.Instant] | [string]
): // @ts-ignore : Commands Used in generated code
typeof Commands & typeof STEPS {
  let time: Temporal.Instant;
  if (Array.isArray(args[0])) {
    time = doyToInstant(String.raw(...(args as [TemplateStringsArray, ...string[]])) as DOY_STRING);
  } else if (typeof args[0] === 'string') {
    time = doyToInstant(args[0] as DOY_STRING);
  } else {
    time = args[0] as Temporal.Instant;
  }

  return commandsWithTimeValue(time, TimingTypes.ABSOLUTE);
}

// @ts-ignore : Used in generated code
function R(
  ...args: [TemplateStringsArray, ...string[]] | [Temporal.Duration] | [string]
): // @ts-ignore : Commands Used in generated code
typeof Commands & typeof STEPS {
  let duration: Temporal.Duration;
  if (Array.isArray(args[0])) {
    duration = hmsToDuration(String.raw(...(args as [TemplateStringsArray, ...string[]])) as HMS_STRING);
  } else if (typeof args[0] === 'string') {
    duration = hmsToDuration(args[0] as HMS_STRING);
  } else {
    duration = args[0] as Temporal.Duration;
  }

  return commandsWithTimeValue(duration, TimingTypes.COMMAND_RELATIVE);
}

// @ts-ignore : Used in generated code
function E(
  ...args: [TemplateStringsArray, ...string[]] | [Temporal.Duration] | [string]
): // @ts-ignore : Commands Used in generated code
typeof Commands & typeof STEPS {
  let duration: Temporal.Duration;
  if (Array.isArray(args[0])) {
    duration = hmsToDuration(String.raw(...(args as [TemplateStringsArray, ...string[]])) as HMS_STRING);
  } else if (typeof args[0] === 'string') {
    duration = hmsToDuration(args[0] as HMS_STRING);
  } else {
    duration = args[0] as Temporal.Duration;
  }
  return commandsWithTimeValue(duration, TimingTypes.EPOCH_RELATIVE);
}

function commandsWithTimeValue<T extends TimingTypes>(
  timeValue: Temporal.Instant | Temporal.Duration,
  timeType: T,
  // @ts-ignore : Commands Used in generated code
): typeof Commands & typeof STEPS {
  return {
    // @ts-ignore : Commands Used in generated code
    ...Object.keys(Commands).reduce((accum, key) => {
      // @ts-ignore : Used in generated code
      const command = Commands[key as keyof Commands];

      if (typeof command === 'function') {
        //if (timeType === TimingTypes.ABSOLUTE) {
        accum[key] = (...args: Parameters<typeof command>): typeof command => {
          switch (timeType) {
            case TimingTypes.ABSOLUTE:
              return command(...args).absoluteTiming(timeValue);
            case TimingTypes.COMMAND_RELATIVE:
              return command(...args).relativeTiming(timeValue);
            case TimingTypes.EPOCH_RELATIVE:
              return command(...args).epochTiming(timeValue);
          }
        };
      } else {
        switch (timeType) {
          case TimingTypes.ABSOLUTE:
            accum[key] = command.absoluteTiming(timeValue);
            break;
          case TimingTypes.COMMAND_RELATIVE:
            accum[key] = command.relativeTiming(timeValue);
            break;
          case TimingTypes.EPOCH_RELATIVE:
            accum[key] = command.epochTiming(timeValue);
            break;
        }
      }

      return accum;
      // @ts-ignore : Used in generated code
    }, {} as typeof Commands),
    ...Object.keys(STEPS).reduce((accum, key) => {
      // @ts-ignore : Used in generated code
      const step = STEPS[key as keyof STEPS];
      // @ts-ignore : Used in generated code
      accum[key] = (...args: Parameters<typeof step>): typeof step => {
        switch (timeType) {
          case TimingTypes.ABSOLUTE:
            return step(...args).absoluteTiming(timeValue);
          case TimingTypes.COMMAND_RELATIVE:
            return step(...args).relativeTiming(timeValue);
          case TimingTypes.EPOCH_RELATIVE:
            return step(...args).epochTiming(timeValue);
        }
      };

      return accum;
    }, {} as typeof STEPS),
  };
}

/*
	  ---------------------------------
			  Utility Functions
	  ---------------------------------
	  */

// @ts-ignore : Used in generated code
function sortCommandArguments(args: { [argName: string]: any }, order: string[]): { [argName: string]: any } {
  if (typeof args[0] === 'object') {
    return Object.keys(args[0])
      .sort((a, b) => order.indexOf(a) - order.indexOf(b))
      .reduce((objectEntries: { [argName: string]: any }, key) => {
        if (Array.isArray(args[0][key])) {
          const sortedRepeatArgs = [];

          for (const test of args[0][key]) {
            sortedRepeatArgs.push(sortCommandArguments([test], order));
          }

          objectEntries[key] = sortedRepeatArgs;
        } else {
          objectEntries[key] = args[0][key];
        }

        return objectEntries;
      }, {});
  }

  return args;
}

function indent(text: string, numTimes: number = 1, char: string = '  '): string {
  return text
    .split('\n')
    .map(line => char.repeat(numTimes) + line)
    .join('\n');
}

// @ts-ignore : 'Args' found in JSON Spec
function argumentsToString<A extends Args[] | { [argName: string]: any } = [] | {}>(args: A): string {
  if (Array.isArray(args)) {
    const argStrings = args.map(arg => {
      if (typeof arg === 'string') {
        return `'${arg}'`;
      }
      return arg.toString();
    });

    return argStrings.join(', ');
  } else {
    return objectToString(args);
  }
}

/**
 * This function takes an array of Args interfaces and converts it into an object.
 * The interfaces array contains objects matching the ARGS interface.
 * Depending on the type property of each object, a corresponding object with the
 * name and value properties is created and added to the output.
 * Additionally, the function includes a validation function that prevents remote
 * property injection attacks.
 * @param interfaces
 */
// @ts-ignore : `Args` found in JSON Spec
function convertInterfacesToArgs(interfaces: Args): {} | [] {
  const args = interfaces.length === 0 ? [] : {};

  // Use to prevent a Remote property injection attack
  const validate = (input: string): boolean => {
    const pattern = /^[a-zA-Z0-9_-]+$/;
    const isValid = pattern.test(input);
    return isValid;
  };

  const convertedArgs = interfaces.map(
    (
      // @ts-ignore : found in JSON Spec
      arg: StringArgument | NumberArgument | BooleanArgument | SymbolArgument | HexArgument | RepeatArgument,
    ) => {
      // @ts-ignore : 'RepeatArgument' found in JSON Spec
      if (arg.type === 'repeat') {
        if (validate(arg.name)) {
          // @ts-ignore : 'RepeatArgument' found in JSON Spec
          return {
            [arg.name]: arg.value.map(
              (
                // @ts-ignore : found in JSON Spec
                repeatArgBundle: (StringArgument | NumberArgument | BooleanArgument | SymbolArgument | HexArgument)[],
              ) =>
                repeatArgBundle.reduce((obj, item) => {
                  if (validate(item.name)) {
                    obj[item.name] = item.value;
                  }
                  return obj;
                }, {}),
            ),
          };
        }
        return { repeat_error: 'Remote property injection detected...' };
      } else if (arg.type === 'symbol') {
        if (validate(arg.name)) {
          // @ts-ignore : 'SymbolArgument' found in JSON Spec
          return { [arg.name]: { symbol: arg.value } };
        }
        return { symbol_error: 'Remote property injection detected...' };
        // @ts-ignore : 'HexArgument' found in JSON Spec
      } else if (arg.type === 'hex') {
        if (validate(arg.name)) {
          // @ts-ignore : 'HexArgument' found in JSON Spec
          return { [arg.name]: { hex: arg.value } };
        }
        return { hex_error: 'Remote property injection detected...' };
      } else {
        if (validate(arg.name)) {
          return { [arg.name]: arg.value };
        }
        return { error: 'Remote property injection detected...' };
      }
    },
  );

  for (const key in convertedArgs) {
    Object.assign(args, convertedArgs[key]);
  }

  return args;
}

/**
 * The specific function to handle repeat args, we need to do this separately because
 * you cannot have a RepeatArgument inside a RepeatArgument.
 *
 * @param args
 * @returns
 */
function convertRepeatArgs(args: { [argName: string]: any }): any[] {
  let result: any[] = [];

  if (args['length'] === 0) {
    return result;
  }

  const values = Array.isArray(args) ? args[0] : args;

  for (let key in values) {
    result.push(convertValueToObject(values[key], key));
  }

  return result;
}

/**
 * This function takes a value and key and converts it to the correct object type supported by the seqjson spec.
 * The only type not supported here is RepeatArgument, as that is handled differently because you cannot have a
 * RepeatArgument inside a RepeatArgument.
 *
 * @param value
 * @param key
 * @returns An object for each type
 */
function convertValueToObject(value: any, key: string): any {
  switch (typeof value) {
    case 'string':
      return { type: 'string', value: value, name: key };
    case 'number':
      return { type: 'number', value: value, name: key };
    case 'boolean':
      return { type: 'boolean', value: value, name: key };
    default:
      if (value instanceof Object && value.symbol && value.symbol === 'string') {
        return { type: 'symbol', value: value, name: key };
      } else if (
        value instanceof Object &&
        value.hex &&
        value.hex === 'string' &&
        new RegExp('^0x([0-9A-F])+$').test(value.hex)
      ) {
        return { type: 'hex', value: value, name: key };
      }
  }
}

/**
 * This method takes an object and converts it to a string representation, with each
 * key-value pair on a new line and nested objects/arrays indented. The indentLevel
 * parameter specifies the initial indentation level, used to prettify the generated
 * eDSL from SeqJSON.
 * @param obj
 * @param indentLevel
 */
function objectToString(obj: any, indentLevel: number = 1): string {
  let output = '';

  const print = (obj: any) => {
    Object.keys(obj).forEach(key => {
      const value = obj[key];

      if (Array.isArray(value)) {
        output += indent(`${key}: [`, indentLevel) + '\n';
        indentLevel++;
        value.forEach((item: any) => {
          output += indent(`{`, indentLevel) + '\n';
          indentLevel++;
          print(item);
          indentLevel--;
          output += indent(`},`, indentLevel) + '\n';
        });
        indentLevel--;
        output += indent(`],`, indentLevel) + '\n';
      } else if (typeof value === 'object') {
        output += indent(`${key}:{`, indentLevel) + '\n';
        indentLevel++;
        print(value);
        indentLevel--;
        output += indent(`},`, indentLevel) + '\n';
      } else {
        output += indent(`${key}: ${typeof value === 'string' ? `'${value}'` : value},`, indentLevel) + '\n';
      }
    });
  };

  output += '{\n';
  print(obj);
  output += `}`;

  return output;
}

/** END Preface */
