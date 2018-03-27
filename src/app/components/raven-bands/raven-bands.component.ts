/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
  SimpleChanges,
} from '@angular/core';
import { OnChanges, OnInit } from '@angular/core/src/metadata/lifecycle_hooks';
import { SortablejsOptions } from 'angular-sortablejs';

import {
  RavenCompositeBand,
  RavenSortMessage,
  RavenSubBand,
  RavenTimeRange,
  StringTMap,
} from './../../shared/models';

export interface BandClickEvent extends Event {
  detail: StringTMap<string>;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'raven-bands',
  styleUrls: ['./raven-bands.component.css'],
  templateUrl: './raven-bands.component.html',
})
export class RavenBandsComponent implements OnChanges, OnInit {
  @Input() bands: RavenCompositeBand[];
  @Input() containerId: string;
  @Input() labelWidth: number;
  @Input() maxTimeRange: RavenTimeRange;
  @Input() selectedBandId: string;
  @Input() viewTimeRange: RavenTimeRange;

  @Output() bandClick: EventEmitter<string> = new EventEmitter<string>();
  @Output() dataPointClick: EventEmitter<any> = new EventEmitter<any>();
  @Output() newSort: EventEmitter<StringTMap<RavenSortMessage>> = new EventEmitter<StringTMap<RavenSortMessage>>();

  sortablejsOptions: SortablejsOptions;
  sortedAndFilteredBands: RavenCompositeBand[];

  ngOnInit() {
    this.sortablejsOptions = {
      animation: 0,
      delay: 0,
      ghostClass: 'sortable-placeholder',
      group: 'bands',
      onAdd: this.onSort.bind(this),
      onEnd: this.onSort.bind(this),
      onRemove: this.onSort.bind(this),
      scroll: true,
      scrollSensitivity: 30,
      scrollSpeed: 10,
      sort: true,
    };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.bands) {
      this.sortedAndFilteredBands =
        [...this.bands]
          .filter(band => band.containerId === this.containerId)
          .sort((a, b) => a.sortOrder - b.sortOrder);
    }
  }

  /**
   * trackBy for bands list.
   * Returns a custom id that is just the band id concatenated with all the subBand ids,
   * separated by a forward-slash.
   * This is so anytime subBands change (i.e. added/removed) we re-render the band.
   */
  bandsTrackByFn(index: number, item: RavenCompositeBand) {
    let id = item.id;

    for (let i = 0, l = item.subBands.length; i < l; ++i) {
      id += `/${item.subBands[i].id}`;
    }

    return id;
  }

  /**
   * trackBy for subBands list.
   */
  subBandsTrackByFn(index: number, item: RavenSubBand) {
    return item.id;
  }

  /**
   * Event. Called when a `falcon-band-click` event is fired from a falcon band.
   */
  @HostListener('falcon-band-click', ['$event'])
  onBandClick(e: BandClickEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.bandClick.emit(e.detail.bandId);
  }

  @HostListener('falcon-composite-band-left-click', ['$event'])
  onDataItemLeftClick(e: BandClickEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.dataPointClick.emit(e.detail);
  }

  /**
   * Helper to sort bands after a sortablejs message.
   * By the time sortedAndFiltered bands gets to this function they should be in their new order.
   * We use that new order to build a dictionary of bands by id to update the store.
   *
   * TODO: Replace 'any' with a concrete type.
   */
  onSort(e: any) {
    const sort: StringTMap<RavenSortMessage> = {};

    for (let i = 0, l = this.sortedAndFilteredBands.length; i < l; ++i) {
      const band = this.sortedAndFilteredBands[i];

      sort[band.id] = {
        containerId: this.containerId,
        sortOrder: i,
      };
    }

    if (Object.keys(sort).length) {
      this.newSort.emit(sort);
    }
  }
}
