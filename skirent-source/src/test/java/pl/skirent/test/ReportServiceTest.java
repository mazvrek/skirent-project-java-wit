package pl.skirent.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.skirent.model.Rental;
import pl.skirent.model.Ski;
import pl.skirent.repository.RentalRepository;
import pl.skirent.repository.SkiRepository;
import pl.skirent.service.RentalService;
import pl.skirent.service.ReportService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReportService}.
 * Tests: getAvailableSkis, getCurrentlyRentedSkis, getOverdueRentals.
 */
public class ReportServiceTest {

    /** Ski repository shared across methods. */
    private SkiRepository skiRepository;

    /** Rental repository shared across methods. */
    private RentalRepository rentalRepository;

    /** Report service under test. */
    private ReportService reportService;

    /** Rental service used to create test rentals. */
    private RentalService rentalService;

    /**
     * Sets up fresh repositories and services before each test.
     */
    @BeforeEach
    public void setUp() {
        skiRepository = new SkiRepository();
        rentalRepository = new RentalRepository();
        reportService = new ReportService(skiRepository, rentalRepository);
        rentalService = new RentalService(rentalRepository);
    }

    /**
     * Tests that getAvailableSkis returns only skis without an active current rental.
     */
    @Test
    public void testGetAvailableSkis() {
        // Add two skis
        Ski ski1 = new Ski(0, 1, "Rossignol", "Hero", "Look", 170.0);
        Ski ski2 = new Ski(0, 1, "Atomic", "Redster", "Atomic", 165.0);
        skiRepository.add(ski1);
        skiRepository.add(ski2);

        // Rent ski1 (currently active: started 1 hour ago, ends 5 hours from now)
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to   = LocalDateTime.now().plusHours(5);
        rentalService.createRental(1, List.of(ski1.getId()), from, to);

        List<Ski> available = reportService.getAvailableSkis();
        assertEquals(1, available.size());
        assertEquals(ski2.getId(), available.get(0).getId());
    }

    /**
     * Tests that getCurrentlyRentedSkis returns only the skis on active rentals right now.
     */
    @Test
    public void testGetCurrentlyRentedSkis() {
        Ski ski1 = new Ski(0, 1, "Rossignol", "Hero", "Look", 170.0);
        Ski ski2 = new Ski(0, 1, "Atomic", "Redster", "Atomic", 165.0);
        skiRepository.add(ski1);
        skiRepository.add(ski2);

        // Rent ski1 currently
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to   = LocalDateTime.now().plusHours(5);
        rentalService.createRental(1, List.of(ski1.getId()), from, to);

        List<Ski> rented = reportService.getCurrentlyRentedSkis();
        assertEquals(1, rented.size());
        assertEquals(ski1.getId(), rented.get(0).getId());
    }

    /**
     * Tests that getOverdueRentals returns only rentals with status "LATE".
     */
    @Test
    public void testGetOverdueRentals() {
        Ski ski1 = new Ski(0, 1, "Brand", "Model", "Bindings", 160.0);
        skiRepository.add(ski1);

        // Past rental – will be marked LATE
        LocalDateTime pastFrom = LocalDateTime.now().minusDays(5);
        LocalDateTime pastTo   = LocalDateTime.now().minusDays(1);
        Rental r = rentalService.createRental(1, List.of(ski1.getId()), pastFrom, pastTo);

        // Run overdue check
        rentalService.checkOverdue();

        List<Rental> overdue = reportService.getOverdueRentals();
        assertEquals(1, overdue.size());
        assertEquals(r.getId(), overdue.get(0).getId());
        assertEquals("LATE", overdue.get(0).getStatus());
    }

    /**
     * Tests that getAvailableSkis returns all skis when nothing is rented.
     */
    @Test
    public void testAllAvailableWhenNothingRented() {
        skiRepository.add(new Ski(0, 1, "A", "B", "C", 150.0));
        skiRepository.add(new Ski(0, 1, "D", "E", "F", 155.0));

        List<Ski> available = reportService.getAvailableSkis();
        assertEquals(2, available.size());
    }

    /**
     * Tests that getOverdueRentals returns empty list when no rentals are overdue.
     */
    @Test
    public void testNoOverdueRentals() {
        List<Rental> overdue = reportService.getOverdueRentals();
        assertTrue(overdue.isEmpty());
    }
}
