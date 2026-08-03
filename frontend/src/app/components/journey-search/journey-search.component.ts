import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Station } from '../../models/booking.models';
import { StationService } from '../../services/station.service';

@Component({
  selector: 'app-journey-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './journey-search.component.html',
  styleUrl: './journey-search.component.scss'
})
export class JourneySearchComponent implements OnInit {
  stations: Station[] = [];
  fromStationId: number | null = null;
  toStationId: number | null = null;
  selectedDate: string = new Date().toISOString().split('T')[0];

  private lastSearchedFrom: number | null = null;
  private lastSearchedTo: number | null = null;
  private lastSearchedDate: string | null = null;

  @Output() search = new EventEmitter<{ from: number; to: number; date: string }>();
  @Output() selectionsStale = new EventEmitter<boolean>();

  constructor(private stationService: StationService) {}

  ngOnInit(): void {
    this.stationService.getAllStations().subscribe(stations => {
      this.stations = stations;
    });
  }

  onSelectionChange(): void {
    const isStale =
      this.lastSearchedFrom !== null &&
      (this.fromStationId !== this.lastSearchedFrom ||
       this.toStationId !== this.lastSearchedTo ||
       this.selectedDate !== this.lastSearchedDate);
    this.selectionsStale.emit(isStale);
  }

  onSearch(): void {
    if (this.isValid) {
      this.lastSearchedFrom = this.fromStationId;
      this.lastSearchedTo = this.toStationId;
      this.lastSearchedDate = this.selectedDate;
      this.selectionsStale.emit(false);
      this.search.emit({ from: this.fromStationId!, to: this.toStationId!, date: this.selectedDate });
    }
  }

  get isValid(): boolean {
    return !!this.fromStationId && !!this.toStationId && this.fromStationId !== this.toStationId && !!this.selectedDate;
  }
}