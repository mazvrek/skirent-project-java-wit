package pl.skirent.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.skirent.model.Rental;
import pl.skirent.repository.RentalRepository;
import pl.skirent.service.RentalService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RentalService}.
 * Tests: createRental (success and date-collision failure), returnRental, checkOverdue.
 */
public class RentalServiceTest {

    /** Repository shared across test methods. */
    private RentalRepository rentalRepository;

    /** Service under test. */
    private RentalService rentalService;

    /** Fixed time base for deterministic tests. */
    private static final LocalDateTime BASE = LocalDateTime.of(2024, 6, 1, 10, 0);

    /**
     * Initialises fresh repository and service before each test.
     */
    @BeforeEach
    public void setUp() {
        rentalRepository = new RentalRepository();
        rentalService = new RentalService(rentalRepository);
    }

    /**
     * Tests that createRental succeeds when the requested skis are available.
     */
    @Test
    public void testCreateRentalSuccess() {
        Rental r = rentalService.createRental(1, List.of(10, 20), BASE, BASE.plusDays(3));
        assertNotNull(r);
        assertEquals(1, r.getId());
        assertEquals("ACTIVE", r.getStatus());
        assertEquals(1, r.getCustomerId());
        assertEquals(List.of(10, 20), r.getSkiIds());
    }

    /**
     * Tests that createRental throws {@link IllegalStateException} when a ski is already
     * on an active rental overlapping the requested dates.
     */
    @Test
    public void testCreateRentalThrowsOnConflict() {
        // First rental: ski 10 is rented from BASE to BASE+3 days
        rentalService.createRental(1, List.of(10), BASE, BASE.plusDays(3));

        // Second rental: same ski 10, overlapping period -> should fail
        assertThrows(IllegalStateException.class, () ->
                rentalService.createRental(2, List.of(10), BASE.plusDays(1), BASE.plusDays(5)));
    }

    /**
     * Tests that createRental succeeds for non-overlapping date ranges on the same ski.
     */
    @Test
    public void testCreateRentalNoConflictAfterEnd() {
        rentalService.createRental(1, List.of(10), BASE, BASE.plusDays(3));
        // Non-overlapping window: starts after the first rental ends
        Rental second = rentalService.createRental(2, List.of(10), BASE.plusDays(3), BASE.plusDays(6));
        assertNotNull(second);
        assertEquals("ACTIVE", second.getStatus());
    }

    /**
     * Tests that returnRental sets status to "RETURNED" and stores remarks.
     */
    @Test
    public void testReturnRental() {
        Rental r = rentalService.createRental(1, List.of(5), BASE, BASE.plusDays(2));
        rentalService.returnRental(r.getId(), "No damage");
        Rental updated = rentalRepository.findById(r.getId());
        assertEquals("RETURNED", updated.getStatus());
        assertEquals("No damage", updated.getRemarks());
    }

    /**
     * Tests that checkOverdue marks past-due active rentals as "LATE".
     */
    @Test
    public void testCheckOverdue() {
        // Create a rental that ended in the past
        LocalDateTime pastFrom = LocalDateTime.now().minusDays(5);
        LocalDateTime pastTo   = LocalDateTime.now().minusDays(1);
        Rental r = rentalService.createRental(1, List.of(99), pastFrom, pastTo);

        rentalService.checkOverdue();

        Rental updated = rentalRepository.findById(r.getId());
        assertEquals("LATE", updated.getStatus());
    }

    /**
     * Tests that checkOverdue does not touch rentals that are still within their period.
     */
    @Test
    public void testCheckOverdueDoesNotAffectFutureRental() {
        LocalDateTime futureFrom = LocalDateTime.now().plusDays(1);
        LocalDateTime futureTo   = LocalDateTime.now().plusDays(5);
        Rental r = rentalService.createRental(1, List.of(50), futureFrom, futureTo);

        rentalService.checkOverdue();

        Rental updated = rentalRepository.findById(r.getId());
        assertEquals("ACTIVE", updated.getStatus());
    }
}
