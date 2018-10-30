/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import {
  RavenActivityTypeFormDialogModule,
  RavenPlanFormDialogModule,
} from '../shared/components/modules';
import { HawkAppModule } from './containers/hawk-app/hawk-app.module';
import { AdaptationEffects } from './effects/adaptation.effects';
import { PlanEffects } from './effects/plan.effects';
import { HawkRoutingModule } from './hawk-routing.module';
import { reducers } from './hawk-store';
import * as fromAdaptation from './reducers/adaptation.reducer';

@NgModule({
  imports: [
    HttpClientModule,
    HawkRoutingModule,
    StoreModule.forFeature('hawk', reducers),
    EffectsModule.forFeature([AdaptationEffects, PlanEffects]),
    HawkAppModule,
    RavenActivityTypeFormDialogModule,
    RavenPlanFormDialogModule,
    StoreModule.forFeature('adaptation', fromAdaptation.reducer),
  ],
})
export class HawkModule {}
