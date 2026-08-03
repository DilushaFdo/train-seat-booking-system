package com.example.train_seat_booking_app;

import com.example.train_seat_booking_app.dto.BookingRequest;
import com.example.train_seat_booking_app.exception.SeatUnavailableException;
import com.example.train_seat_booking_app.models.Booking;
import com.example.train_seat_booking_app.repository.BookingRepository;
import com.example.train_seat_booking_app.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TrainSeatBookingAppApplicationTests {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	private static final Long SEAT_ID = 50L;
	private static final Long TRIP_ID = 1L;

	@BeforeEach
	void cleanUp() {
		List<Booking> existing = bookingRepository.findAllBySeatAndTrip(SEAT_ID, TRIP_ID);
		bookingRepository.deleteAll(existing);
	}

	@Test
	void onlyOneOfTwoConcurrentOverlappingBookingsShouldSucceed() throws InterruptedException {
		// Assumes trip id 1 and seat id
		Long tripId = 1L;
		Long seatId = 50L;
		Long fromStationId = 1L; // Colombo Fort
		Long toStationId = 8L;   // Kandy

		int threadCount = 5;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch readyLatch = new CountDownLatch(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		List<Future<Boolean>> results = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			int passengerIndex = i;
			Future<Boolean> future = executor.submit(() -> {
				readyLatch.countDown();
				startLatch.await(); // all threads wait here, then fire together
				try {
					BookingRequest request = new BookingRequest();
					request.setTripId(tripId);
					request.setSeatId(seatId);
					request.setFromStationId(fromStationId);
					request.setToStationId(toStationId);
					request.setPassengerName("Concurrent Passenger " + passengerIndex);

					bookingService.createBooking(request);
					return true; // succeeded
				} catch (SeatUnavailableException ex) {
					return false; // correctly rejected
				}
			});
			results.add(future);
		}

		readyLatch.await(); // wait until all threads are ready
		startLatch.countDown(); // release them all at once

		long successCount = 0;
		for (Future<Boolean> result : results) {
			try {
				if (result.get()) {
					successCount++;
				}
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
		executor.shutdown();

		assertEquals(1, successCount, "Exactly one concurrent booking should succeed for an overlapping segment");
	}

}
