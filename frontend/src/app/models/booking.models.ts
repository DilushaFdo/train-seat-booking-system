export interface Station {
  id: number;
  name: string;
  sequenceOrder: number;
  distanceKm: number;
}

export interface SeatAvailability {
  seatId: number;
  coachNumber: string;
  seatNumber: string;
  fare: number;
  available: boolean;
}

export interface BookingRequest {
  tripId: number;
  seatId: number;
  fromStationId: number;
  toStationId: number;
  passengerName: string;
}

export interface BookingResponse {
  bookingId: number;
  passengerName: string;
  coachNumber: string;
  seatNumber: string;
  originStation: string;
  destinationStation: string;
  fare: number;
  status: string;
}