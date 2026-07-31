import { Component } from '@angular/core';
import { JourneySearchComponent } from "./components/journey-search/journey-search.component";
import { SeatAvailability } from './models/booking.models';
import { SeatMapComponent } from "./components/seat-map/seat-map.component";
import { BookingDialogComponent } from "./components/booking-dialog/booking-dialog.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [JourneySearchComponent, SeatMapComponent, BookingDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  tripId = 1;
  fromStationId?: number;
  toStationId?: number;
  selectedSeat: SeatAvailability | null = null;
  refreshKey = 0; // used to force seat-map to reload after a booking

  onSearch(event: { from: number; to: number }): void {
    this.fromStationId = event.from;
    this.toStationId = event.to;
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
}
