import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SeatAvailability, BookingRequest, BookingResponse } from '../../models/booking.models';
import { BookingService } from '../../services/booking.service';

@Component({
  selector: 'app-booking-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-dialog.component.html',
  styleUrl: './booking-dialog.component.scss'
})
export class BookingDialogComponent {
  @Input() tripId!: number;
  @Input() fromStationId!: number;
  @Input() toStationId!: number;
  @Input() seat!: SeatAvailability;

  @Output() closed = new EventEmitter<void>();
  @Output() bookingConfirmed = new EventEmitter<void>();

  passengerName = '';
  submitting = false;
  errorMessage: string | null = null;
  confirmedBooking: BookingResponse | null = null;

  constructor(private bookingService: BookingService) {}

  confirm(): void {
    if (!this.passengerName.trim()) return;

    this.submitting = true;
    this.errorMessage = null;

    const request: BookingRequest = {
      tripId: this.tripId,
      seatId: this.seat.seatId,
      fromStationId: this.fromStationId,
      toStationId: this.toStationId,
      passengerName: this.passengerName.trim()
    };

    this.bookingService.createBooking(request).subscribe({
      next: (response) => {
        this.confirmedBooking = response;
        this.submitting = false;
        this.bookingConfirmed.emit();
      },
      error: (err) => {
        this.submitting = false;
        if (err.status === 409) {
          this.errorMessage = err.error ?? 'This seat was just booked by someone else for an overlapping segment.';
        } else if (err.status === 400) {
          this.errorMessage = err.error ?? 'Invalid booking request.';
        } else {
          this.errorMessage = 'Something went wrong. Please try again.';
        }
      }
    });
  }

  close(): void {
    this.closed.emit();
  }
}