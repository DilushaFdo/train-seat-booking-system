# Segment-Based Train Seat Booking System

A train ticket booking system for Sri Lanka Railways' Colombo Fort-Badulla line,
built around a core idea: a single reserved seat can be booked independently
for multiple non-overlapping legs of a journey, rather than being locked to
one passenger for an entire trip. For example, the same seat can be sold to
one passenger from Colombo Fort to Kandy, and to a different passenger from
Kandy to Badulla on that same service - as long as their segments don't overlap.

The system enforces this safely even when multiple booking requests happen
concurrently for the same seat.

## Tech Stack

- **Backend:** Spring Boot 4 (Java 21), Spring Data JPA, PostgreSQL
- **Frontend:** Angular (standalone components), TypeScript
- **Database:** PostgreSQL 16
- **Infrastructure:** Docker & Docker Compose

## How to Run

**Prerequisites:** Docker Desktop installed and running.

1. Clone the repository.
2. Copy `.env.example` to `.env` at the repo root (default values work out of the box for local use):
3. From the repo root, run:
4. Once all containers are up, open:
5. On first startup, the backend automatically seeds the database with the
   real Colombo Fort-Badulla station list, trains, routes, coaches,
   reserved seats, and scheduled trips with departure and arrival times -
   so the app is immediately usable with no manual data entry.

No manual database setup, migrations, or seed scripts need to be run separately -
everything is handled automatically on `docker compose up`.

## User Flow

1. User selects:
   - Origin station
   - Destination station
   - Travel date

2. The system displays available trips including:
   - Train name
   - Departure time
   - Arrival time

3. User selects a specific scheduled trip.

4. The system displays available reserved seats based on existing bookings
   for that trip.

5. User selects a seat and completes the booking.

The user selects a Trip rather than a Train because the same train can operate
multiple scheduled journeys on the same day.

## Design Decisions & Alternatives Considered

### Segment-based seat modeling

Each train route stores stations in their travelling order using a
TrainRoute entity with a stopOrder value. Each booking stores the origin and
destination stop positions as a range (`[originSeq, destinationSeq)`).Two bookings on the same seat conflict
only if their ranges overlap - this allows the same seat to be sold to multiple
passengers for different, non-overlapping legs of the same trip, which is the
core requirement of the system. The route order is maintained per train, allowing different trains to stop at
different stations while sharing common stations in the network.

### Train vs Trip separation

A Train represents the physical train, while a Trip represents a scheduled
journey operated by that train on a specific date and time.

This separation allows the same train to operate multiple services in a day.

For example:

Train:
Udarata Menike

Trips:
- 05:55 Colombo Fort → Badulla
- 20:30 Colombo Fort → Badulla

Bookings are linked to a Trip rather than directly to a Train. This ensures
that seat availability and reservations are calculated for the correct
scheduled journey.

### Concurrency safety: pessimistic locking vs. a database-level constraint

Two passengers could attempt to book overlapping segments on the same seat at
the same instant. I considered two approaches:

1. **A PostgreSQL exclusion constraint** (`EXCLUDE USING gist`), which would
   make it physically impossible for overlapping ranges to exist in the
   `bookings` table for the same seat and trip, regardless of application-level
   race conditions.
2. **Application-level pessimistic locking** - using `SELECT ... FOR UPDATE`
   to lock existing bookings for a given seat and trip inside a transaction,
   check for overlap in Java, and only then insert.

I initially built the schema around option 1, using Flyway migrations. However,
implementing it meant giving up Hibernate's automatic schema management
(`ddl-auto: update`), since JPA has no annotation equivalent for a PostgreSQL
exclusion constraint - the schema would have to be fully hand-written and
maintained in SQL going forward. Given the project timeline, I chose to switch
to Hibernate-managed schema generation and enforce the conflict-prevention rule
in the application layer instead, using pessimistic locking within a
`@Transactional` service method. This is a slightly weaker guarantee in theory
(correctness depends on correct application code rather than a database-enforced
invariant), but it was the more pragmatic choice given the time available, and
it is still fully safe under concurrent requests because the lock is held for
the duration of the check-and-insert transaction.

If I were to continue this project, reintroducing the exclusion constraint
(via Flyway, alongside Hibernate in validate-only mode) would be the first
thing I'd add back for stronger guarantees.

### Concurrency testing uncovered a real race condition

To verify the locking strategy actually holds under real concurrent load (not
just sequential manual testing), I wrote an automated test that fires 5
simultaneous booking requests at the same seat for the same overlapping
segment, using two `CountDownLatch`es to force genuine simultaneity rather
than sequential thread execution.

The first version of this test failed: all 5 requests succeeded, when only 1
should have. The root cause was a phantom-read scenario - my original locking
query (`SELECT ... FOR UPDATE` on existing bookings for a given seat and trip)
only locks rows that already exist. For a seat's very first booking, there are
zero existing rows to lock, so the lock protected nothing, and all 5 concurrent
transactions read "no conflict" and inserted successfully.

The fix was to lock the `Seat` entity itself instead of the (possibly empty)
set of existing bookings for it. A seat row always exists, so locking it
directly guarantees every booking attempt for that seat is fully serialized,
regardless of whether any prior bookings exist. After this change, the same
test consistently passes - exactly 1 of 5 simultaneous overlapping requests
succeeds, the other 4 correctly receive a conflict response.

This was a valuable reminder that manual, one-request-at-a-time testing can
give false confidence about concurrency correctness - the bug was only caught
because of an automated test specifically designed to create real contention.

### Denormalized sequence numbers on the Booking entity

Each booking stores `originSeq`/`destinationSeq` as plain integers, in addition
to `originStationId`/`destinationStationId`.

Strictly, this introduces controlled denormalization because these values can
be derived from the train route configuration (`TrainRoute.stopOrder`). I chose
to store them because the seat conflict detection logic works by comparing
integer ranges directly, avoiding repeated joins to the route tables during
availability checks and booking validation.

The values are captured at booking time, representing the passenger's journey
segment for that specific trip. Since route configurations are expected to be
stable once trips are published, the risk of these stored values becoming
inconsistent is low. If route editing after bookings becomes a requirement,
additional safeguards such as immutable route versions or validation checks
would be introduced.

### Unreserved coaches excluded from seat-level booking

The system models two coach types: RESERVED and UNRESERVED. Only RESERVED
coaches have individual `Seat` rows and go through the booking/conflict-checking
logic. UNRESERVED coaches are modeled with just a `seatCount` for
informational/capacity purposes, since they are first-come-first-served with
no seat assignment - applying the same booking logic to them would have added
complexity without meeting any real requirement.

### Fare calculation

Fare is calculated as a base fare plus a flat rate per kilometer, using each
station's stored distance from the origin. This is intentionally simple;
banded or dynamic pricing would be a natural extension but was out of scope
given the project timeline.

### Fare fairness and the segment-based model

The department's stated concern - that a passenger travelling only part of a
route effectively pays for (or causes lost revenue on) the remainder of the
seat's journey, since it historically couldn't be resold - is addressed
structurally, not just by the fare formula itself. Fares are calculated
strictly on distance actually travelled by that passenger. What makes this
fair, rather than a source of lost revenue, is that the segment-based booking
model allows the remaining portion of the same seat's journey (e.g. Kandy to
Badulla, after a Colombo Fort to Kandy booking) to be independently sold to a
different passenger. The booking system is what recovers the revenue leadership
identified as being left on the table - the fare model simply reflects actual
distance travelled, with no passenger subsidizing another's unused segment.

Unreserved coaches are out of scope for this seat-level fare model, since they
have no individual seat assignment and are booked/boarded on a
first-come-first-served basis, consistent with how unreserved travel works in
practice.

### Authentication deferred

The brief encouraged treating this as a real, production-oriented system. I
considered adding user accounts with Spring Security and JWT-based
authentication, so bookings would be tied to a logged-in user instead of a
freely-typed passenger name. I deliberately chose not to implement this, since
it would have been a substantial addition competing directly with time needed
to ensure the core booking logic, concurrency safety, and containerized setup
were solid - which the brief explicitly prioritized over additional features.
See "Future Improvements" below.

## Challenges Faced

- **A concurrency bug only surfaced under automated testing** - see the
  "Concurrency testing uncovered a real race condition" section above for
  full details.

- **Local port conflicts during development.** My machine had a native
  PostgreSQL instance (via pgAdmin) already running on port 5432, and later
  XAMPP's Apache already running on port 80. Both caused confusing connection
  errors when a Dockerized service tried to bind the same host port. Resolved
  by remapping host ports (5433 for local Postgres testing, 8081 for the
  final frontend) while keeping container-internal ports unchanged.

- **`npm ci` failing in the Docker build due to a stale lock file.**
  `package-lock.json` had drifted out of sync with `package.json` (likely
  after an Angular CLI update), causing `npm ci` to fail inside the build
  stage with dependency mismatch errors. Fixed by regenerating the lock file
  locally with `npm install` before rebuilding the image.

- **Choosing between schema-management strategies.** I initially built the
  schema using Flyway migrations specifically to support a PostgreSQL
  exclusion constraint for concurrency safety. Given the project timeline, I
  made the deliberate choice to switch to Hibernate-managed schema generation
  and move the concurrency guarantee into the application layer instead (see
  Design Decisions above) - a good example of balancing an ideal design
  against a hard deadline.

  ## Future Improvements

Given more time, I would prioritize, in this order:

1. **Database-level concurrency guarantee** - reintroduce a PostgreSQL
   exclusion constraint via Flyway migrations (alongside Hibernate in
   validate-only mode) so overlapping bookings are physically impossible at
   the database layer, not just prevented by application logic.
2. **Authentication** - Spring Security with JWT, so bookings are tied to an
   authenticated user rather than a free-text passenger name, with a
   booking history view per user.
3. 3. **Expand automated testing coverage** - add more integration tests covering
   multiple trips per train, route validation, seat availability edge cases,
   and booking cancellation scenarios.
4. **Admin capability** - endpoints and a simple UI for managing stations,
   coaches, and trips, so the system's configurability extends to non-technical
   operators, not just database rows.
5. **Dynamic/banded fare pricing**, replacing the current flat per-km rate.
6. **Trip and schedule management**
   Admin functionality for creating and updating train schedules,
   departure times, arrival times, and route configurations without
   modifying database records manually.