/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Store, StoreModule } from '@ngrx/store';
import { addMatchers, cold, hot, initTestScheduler } from 'jasmine-marbles';
import { Observable, of } from 'rxjs';
import { CommandDictionaryActions } from '../actions';
import { reducers } from '../falcon-store';
import {
  CommandDictionaryMockService,
  mockCommandDictionaryList,
} from '../services/command-dictionary-mock.service';
import { CommandDictionaryEffects } from './command-dictionary.effects';

describe('CommandDictionaryEffects', () => {
  let effects: CommandDictionaryEffects;
  let commandDictionaryMockService: any;
  let actions: Observable<any> = of();

  beforeEach(() => {
    commandDictionaryMockService = jasmine.createSpyObj(
      'CommandDictionaryMockService',
      ['getCommandDictionaryList', 'getCommandDictionary'],
    );

    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        StoreModule.forFeature('falcon', reducers),
      ],
      providers: [
        CommandDictionaryEffects,
        provideMockActions(() => actions),
        {
          provide: CommandDictionaryMockService,
          useValue: commandDictionaryMockService,
        },
        Store,
      ],
    });

    initTestScheduler();
    addMatchers();
    effects = TestBed.get(CommandDictionaryEffects);
  });

  describe('fetchCommandDictionaries', () => {
    it('should return a FetchCommandDictionariesSuccess with data on success', () => {
      const action = CommandDictionaryActions.fetchCommandDictionaries();
      const success = CommandDictionaryActions.fetchCommandDictionariesSuccess({
        data: mockCommandDictionaryList,
      });

      commandDictionaryMockService.getCommandDictionaryList.and.returnValue(
        of(mockCommandDictionaryList),
      );

      actions = hot('--a-', { a: action });
      const expected = cold('--b', { b: success });

      expect(effects.fetchCommandDictionaries).toBeObservable(expected);
    });

    it('should return a FetchCommandDictionariesFailure with error on failure', () => {
      const action = CommandDictionaryActions.fetchCommandDictionaries();
      const error = new Error('MOCK_FAILURE');
      const failure = CommandDictionaryActions.fetchCommandDictionariesFailure({
        error,
      });

      // Make the service return a fake error observable
      commandDictionaryMockService.getCommandDictionaryList.and.returnValue(
        cold('--#|', {}, error),
      );

      actions = hot('-a--', { a: action });

      // We expect the service to return the failure because we have forced the
      // service to fail with the spy
      const expected = cold('---b', { b: failure });

      expect(effects.fetchCommandDictionaries).toBeObservable(expected);
    });
  });

  describe('fetchCommandDictionary', () => {
    it('should return a FetchCommandDictionarySuccess with data on success', () => {
      const name = mockCommandDictionaryList[0].id;
      const action = CommandDictionaryActions.fetchCommandDictionary({ name });
      const success = CommandDictionaryActions.fetchCommandDictionarySuccess({
        data: commandDictionaryMockService.getCommandDictionary(),
      });
      commandDictionaryMockService.getCommandDictionary.and.returnValue(
        of(commandDictionaryMockService.getCommandDictionary()),
      );

      actions = hot('--a-', { a: action });
      const expected = cold('--b', { b: success });

      expect(effects.fetchCommandDictionary).toBeObservable(expected);
    });

    it('should return a FetchCommandDictionaryFailure with error on failure', () => {
      const action = CommandDictionaryActions.fetchCommandDictionary({
        name: mockCommandDictionaryList[0].id,
      });
      const error = new Error('MOCK_FAILURE');
      const failure = CommandDictionaryActions.fetchCommandDictionaryFailure({
        error,
      });

      // Make the service return a fake error observable
      commandDictionaryMockService.getCommandDictionary.and.returnValue(
        cold('--#|', {}, error),
      );

      actions = hot('-a--', { a: action });

      // We expect the service to return the failure because we have forced the
      // service to fail with the spy
      const expected = cold('---b', { b: failure });

      expect(effects.fetchCommandDictionary).toBeObservable(expected);
    });
  });

  describe('selectCommandDictionary', () => {
    it('should return a FetchCommandDictionary with an id', () => {
      const selectedId = '42';
      const action = CommandDictionaryActions.selectCommandDictionary({
        selectedId,
      });
      const success = CommandDictionaryActions.fetchCommandDictionary({
        name: selectedId,
      });

      actions = hot('--a-', { a: action });
      const expected = cold('--b', { b: success });

      expect(effects.selectCommandDictionary).toBeObservable(expected);
    });
  });
});
