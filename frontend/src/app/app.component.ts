import { Component } from '@angular/core';
import { JourneySearchComponent } from "./components/journey-search/journey-search.component";
import { SeatAvailability } from './models/booking.models';
import { SeatMapComponent } from "./components/seat-map/seat-map.component";
import { BookingDialogComponent } from "./components/booking-dialog/booking-dialog.component";
import { TrainSelectComponent } from "./components/train-select/train-select.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [JourneySearchComponent, SeatMapComponent, BookingDialogComponent, TrainSelectComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  fromStationId?: number;
  toStationId?: number;
  selectedTripId: number | null = null;
  selectedSeat: SeatAvailability | null = null;
  selectedDate?: string;
  refreshKey = 0;
  isStale = false;

  onSearch(event: { from: number; to: number; date: string }): void {
  this.fromStationId = event.from;
  this.toStationId = event.to;
  this.selectedDate = event.date;
  this.selectedTripId = null;
  this.isStale = false;
}

  onTripSelected(tripId: number): void {
    this.selectedTripId = tripId;
  }

  onSeatSelected(seat: SeatAvailability): void {
    this.selectedSeat = seat;
  }

  onDialogClosed(): void {
    this.selectedSeat = null;
  }

  onBookingConfirmed(): void {
    this.refreshKey++; // triggers seat-map re-fetch, see below
  }

  onSelectionsStale(stale: boolean): void {
    this.isStale = stale;
  }
}
