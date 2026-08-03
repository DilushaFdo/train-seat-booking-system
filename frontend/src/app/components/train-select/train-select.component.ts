import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TripSearchResult } from '../../models/booking.models';
import { BookingService } from '../../services/booking.service';
import { Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-train-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './train-select.component.html',
  styleUrl: './train-select.component.scss'
})
export class TrainSelectComponent implements OnChanges {
  @Input() fromStationId!: number;
  @Input() toStationId!: number;
  @Input() date!: string;

  @Output() tripSelected = new EventEmitter<number>();

  trips: TripSearchResult[] = [];
  loading = false;
  errorMessage: string | null = null;

  constructor(private bookingService: BookingService) {}

  ngOnChanges(): void {
    if (this.fromStationId && this.toStationId && this.date) {
      this.loading = true;
      this.errorMessage = null;
      this.bookingService.searchTrips(this.fromStationId, this.toStationId, this.date).subscribe({
        next: (trips) => {
          this.trips = trips;
          this.loading = false;
        },
        error: () => {
          this.errorMessage = 'Could not load trains for this route.';
          this.loading = false;
        }
      });
    }
  }

  onSelect(trip: TripSearchResult): void {
    if (trip.status !== 'SCHEDULED') return;
    this.tripSelected.emit(trip.tripId);
  }
}