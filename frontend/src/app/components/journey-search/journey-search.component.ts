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

  @Output() search = new EventEmitter<{ from: number; to: number }>();

  constructor(private stationService: StationService) {}

  ngOnInit(): void {
    this.stationService.getAllStations().subscribe(stations => {
      this.stations = stations;
    });
  }

  onSearch(): void {
    if (this.fromStationId && this.toStationId && this.fromStationId !== this.toStationId) {
      this.search.emit({ from: this.fromStationId, to: this.toStationId });
    }
  }

  get isValid(): boolean {
    return !!this.fromStationId && !!this.toStationId && this.fromStationId !== this.toStationId;
  }
}