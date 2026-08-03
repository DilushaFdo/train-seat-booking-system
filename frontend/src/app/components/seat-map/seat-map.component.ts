import { Component, Input, OnChanges, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SeatAvailability } from '../../models/booking.models';
import { BookingService } from '../../services/booking.service';

interface SeatRow {
  left: SeatAvailability[];
  right: SeatAvailability[];
}

interface CoachGroup {
  coachNumber: string;
  rows: SeatRow[];
}

const SEATS_PER_ROW = 5;
const LEFT_SEATS_PER_ROW = 3;

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
        error: () => {
          this.errorMessage = 'Could not load seat availability. Please try again.';
          this.loading = false;
        }
      });
  }

  private groupByCoach(seats: SeatAvailability[]): CoachGroup[] {
    const byCoach = new Map<string, SeatAvailability[]>();
    for (const seat of seats) {
      if (!byCoach.has(seat.coachNumber)) {
        byCoach.set(seat.coachNumber, []);
      }
      byCoach.get(seat.coachNumber)!.push(seat);
    }

    return Array.from(byCoach.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([coachNumber, coachSeats]) => ({
        coachNumber,
        rows: this.buildRows(coachSeats)
      }));
  }

  private buildRows(seats: SeatAvailability[]): SeatRow[] {
    // Sort numerically by seat number so row order matches physical layout
    const sorted = [...seats].sort((a, b) => Number(a.seatNumber) - Number(b.seatNumber));

    const rows: SeatRow[] = [];
    for (let i = 0; i < sorted.length; i += SEATS_PER_ROW) {
      const chunk = sorted.slice(i, i + SEATS_PER_ROW);
      rows.push({
        left: chunk.slice(0, LEFT_SEATS_PER_ROW),
        right: chunk.slice(LEFT_SEATS_PER_ROW)
      });
    }
    return rows;
  }

  onSeatClick(seat: SeatAvailability): void {
    if (!seat.available) return;
    this.seatSelected.emit(seat);
  }
}