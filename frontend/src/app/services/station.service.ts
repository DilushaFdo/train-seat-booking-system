import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Station } from '../models/booking.models';

@Injectable({ providedIn: 'root' })
export class StationService {
  private baseUrl = '/api/stations';

  constructor(private http: HttpClient) {}

  getAllStations(): Observable<Station[]> {
    return this.http.get<Station[]>(this.baseUrl);
  }
}