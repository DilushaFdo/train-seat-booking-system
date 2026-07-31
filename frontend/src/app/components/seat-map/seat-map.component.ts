import { Component, Input, OnChanges, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SeatAvailability } from '../../models/booking.models';
import { BookingService } from '../../services/booking.service';

interface CoachGroup {
  coachNumber: string;
  seats: SeatAvailability[];
}

@Component({
  selector: 'app-seat-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './seat-map.component.html',
  styleUrl: './seat-map.component.scss'
})
export class SeatMapComponent implements OnChanges {
  @Input() tripId!: number;
  @Input() fromStationId!: number;
  @Input() toStationId!: number;
  @Input() refreshTrigger = 0;

  @Output() seatSelected = new EventEmitter<SeatAvailability>();

  coachGroups: CoachGroup[] = [];
  loading = false;
  errorMessage: string | null = null;

  constructor(private bookingService: BookingService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (this.tripId && this.fromStationId && this.toStationId) {
      this.loadAvailability();
    }
  }

  loadAvailability(): void {
    this.loading = true;
    this.errorMessage = null;

    this.bookingService.getAvailability(this.tripId, this.fromStationId, this.toStationId)
      .subscribe({
        next: (seats) => {
          this.coachGroups = this.groupByCoach(seats);
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = 'Could not load seat availability. Please try again.';
          this.loading = false;
        }
      });
  }

  private groupByCoach(seats: SeatAvailability[]): CoachGroup[] {
    const map = new Map<string, SeatAvailability[]>();
    for (const seat of seats) {
      if (!map.has(seat.coachNumber)) {
        map.set(seat.coachNumber, []);
      }
      map.get(seat.coachNumber)!.push(seat);
    }
    return Array.from(map.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([coachNumber, seats]) => ({ coachNumber, seats }));
  }

  onSeatClick(seat: SeatAvailability): void {
  if (!seat.available) return;
  this.seatSelected.emit(seat);
}
}