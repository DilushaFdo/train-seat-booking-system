import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SeatAvailability, BookingRequest, BookingResponse, TripSearchResult } from '../models/booking.models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  getAvailability(tripId: number, fromStationId: number, toStationId: number): Observable<SeatAvailability[]> {
    return this.http.get<SeatAvailability[]>(
      `${this.baseUrl}/trips/${tripId}/availability`,
      { params: { from: fromStationId, to: toStationId } }
    );
  }

  createBooking(request: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.baseUrl}/bookings`, request);
  }

  searchTrips(fromStationId: number, toStationId: number, date: string): Observable<TripSearchResult[]> {
  return this.http.get<TripSearchResult[]>(`${this.baseUrl}/trips/search`, {
    params: { from: fromStationId, to: toStationId, date }
  });

}
}