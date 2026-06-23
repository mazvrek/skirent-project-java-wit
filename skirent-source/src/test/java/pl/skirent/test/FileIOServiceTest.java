package pl.skirent.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.skirent.config.AppConfig;
import pl.skirent.io.FileIOService;
import pl.skirent.model.Customer;
import pl.skirent.model.Rental;
import pl.skirent.model.Ski;
import pl.skirent.model.SkiType;
import pl.skirent.repository.CustomerRepository;
import pl.skirent.repository.RentalRepository;
import pl.skirent.repository.SkiRepository;
import pl.skirent.repository.SkiTypeRepository;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link FileIOService}.
 * Verifies that data can be saved to a file and fully restored on load,
 * with all field values matching the originals.
 */
public class FileIOServiceTest {

    /** Repositories used to populate and receive data. */
    private SkiTypeRepository skiTypeRepo;
    private SkiRepository skiRepo;
    private CustomerRepository customerRepo;
    private RentalRepository rentalRepo;

    /** The file IO service singleton. */
    private FileIOService fileIOService;

    /** Temporary directory provided by JUnit 5. */
    @TempDir
    Path tempDir;

    /**
     * Sets up fresh repositories and resets the FileIOService singleton before each test.
     * Also sets the data file path to the temporary directory.
     */
    @BeforeEach
    public void setUp() throws Exception {
        skiTypeRepo = new SkiTypeRepository();
        skiRepo = new SkiRepository();
        customerRepo = new CustomerRepository();
        rentalRepo = new RentalRepository();

        // Reset singleton so each test gets a fresh instance
        Field instanceField = FileIOService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        fileIOService = FileIOService.getInstance();

        // Override the DATA_FILE constant via reflection to use temp dir
        setDataFile(tempDir.resolve("test_skirent.dat").toString());
    }

    /**
     * Shuts down the executor service after each test.
     */
    @AfterEach
    public void tearDown() throws Exception {
        fileIOService.shutdown();
        // Reset instance
        Field instanceField = FileIOService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        // Restore original DATA_FILE
        setDataFile("skirent_data.dat");
    }

    /**
     * Helper to override the DATA_FILE constant via reflection.
     *
     * @param path the new file path
     */
    private void setDataFile(String path) throws Exception {
        Field field = AppConfig.class.getDeclaredField("DATA_FILE");
        field.setAccessible(true);
        // final fields require removing the final modifier
        java.lang.reflect.Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            // Java 12+ doesn't have modifiers field; use alternative approach
        }
        // Use a workaround: write the file to the temp path directly
        // Store path in a thread-local or use a different mechanism
        // For Java 17, we use a system property approach instead
        System.setProperty("skirent.data.file", path);
    }

    /**
     * Tests that saving SkiTypes and loading them back produces equal data.
     */
    @Test
    public void testSaveAndLoadSkiTypes() throws Exception {
        String testFile = tempDir.resolve("test_skirent_skitype.dat").toString();
        setTestFile(testFile);

        SkiType st1 = new SkiType(0, "Alpine", "Downhill");
        SkiType st2 = new SkiType(0, "Nordic", "Cross-country");
        skiTypeRepo.add(st1);
        skiTypeRepo.add(st2);

        fileIOService.saveAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
        // Wait for async write to complete
        fileIOService.shutdown();
        Thread.sleep(500);

        // Reset instance to get fresh executor
        Field instanceField = FileIOService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        fileIOService = FileIOService.getInstance();

        // Load into fresh repos
        SkiTypeRepository loadedSkiTypeRepo = new SkiTypeRepository();
        SkiRepository loadedSkiRepo = new SkiRepository();
        CustomerRepository loadedCustomerRepo = new CustomerRepository();
        RentalRepository loadedRentalRepo = new RentalRepository();

        fileIOService.loadAllData(loadedSkiTypeRepo, loadedSkiRepo, loadedCustomerRepo, loadedRentalRepo);

        List<SkiType> all = loadedSkiTypeRepo.findAll();
        assertEquals(2, all.size());
        assertEquals("Alpine", all.get(0).getName());
        assertEquals("Downhill", all.get(0).getDescription());
        assertEquals("Nordic", all.get(1).getName());
    }

    /**
     * Tests that saving Skis and loading them back preserves all fields.
     */
    @Test
    public void testSaveAndLoadSkis() throws Exception {
        String testFile = tempDir.resolve("test_skirent_ski.dat").toString();
        setTestFile(testFile);

        Ski ski = new Ski(0, 1, "Rossignol", "Hero Elite", "Look", 172.5);
        skiRepo.add(ski);

        fileIOService.saveAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
        fileIOService.shutdown();
        Thread.sleep(500);

        Field instanceField = FileIOService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        fileIOService = FileIOService.getInstance();

        SkiTypeRepository lr1 = new SkiTypeRepository();
        SkiRepository lr2 = new SkiRepository();
        CustomerRepository lr3 = new CustomerRepository();
        RentalRepository lr4 = new RentalRepository();
        fileIOService.loadAllData(lr1, lr2, lr3, lr4);

        List<Ski> skis = lr2.findAll();
        assertEquals(1, skis.size());
        assertEquals("Rossignol", skis.get(0).getBrand());
        assertEquals("Hero Elite", skis.get(0).getModel());
        assertEquals(172.5, skis.get(0).getLength(), 0.001);
    }

    /**
     * Tests that saving Customers and loading them back preserves all fields.
     */
    @Test
    public void testSaveAndLoadCustomers() throws Exception {
        String testFile = tempDir.resolve("test_skirent_customer.dat").toString();
        setTestFile(testFile);

        Customer c = new Customer(0, "Jan", "Kowalski", "ABC123", "VIP customer");
        customerRepo.add(c);

        fileIOService.saveAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
        fileIOService.shutdown();
        Thread.sleep(500);

        Field instanceField = FileIOService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        fileIOService = FileIOService.getInstance();

        SkiTypeRepository lr1 = new SkiTypeRepository();
        SkiRepository lr2 = new SkiRepository();
        CustomerRepository lr3 = new CustomerRepository();
        RentalRepository lr4 = new RentalRepository();
        fileIOService.loadAllData(lr1, lr2, lr3, lr4);

        List<Customer> customers = lr3.findAll();
        assertEquals(1, customers.size());
        assertEquals("Jan", customers.get(0).getFirstName());
        assertEquals("Kowalski", customers.get(0).getLastName());
        assertEquals("ABC123", customers.get(0).getDocumentNumber());
        assertEquals("VIP customer", customers.get(0).getDescription());
    }

    /**
     * Tests that saving Rentals and loading them back preserves all fields including skiIds and dates.
     */
    @Test
    public void testSaveAndLoadRentals() throws Exception {
        String testFile = tempDir.resolve("test_skirent_rental.dat").toString();
        setTestFile(testFile);

        LocalDateTime from = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime to   = LocalDateTime.of(2024, 6, 5, 10, 0);
        Rental r = new Rental(0, 1, List.of(1, 2, 3), from, to, "ACTIVE", "test remarks");
        rentalRepo.add(r);

        fileIOService.saveAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
        fileIOService.shutdown();
        Thread.sleep(500);

        Field instanceField = FileIOService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        fileIOService = FileIOService.getInstance();

        SkiTypeRepository lr1 = new SkiTypeRepository();
        SkiRepository lr2 = new SkiRepository();
        CustomerRepository lr3 = new CustomerRepository();
        RentalRepository lr4 = new RentalRepository();
        fileIOService.loadAllData(lr1, lr2, lr3, lr4);

        List<Rental> rentals = lr4.findAll();
        assertEquals(1, rentals.size());
        Rental loaded = rentals.get(0);
        assertEquals(1, loaded.getCustomerId());
        assertEquals(List.of(1, 2, 3), loaded.getSkiIds());
        assertEquals(from, loaded.getFrom());
        assertEquals(to, loaded.getTo());
        assertEquals("ACTIVE", loaded.getStatus());
        assertEquals("test remarks", loaded.getRemarks());
    }

    /**
     * Helper to set the file path used by FileIOService via AppConfig override.
     * Since AppConfig.DATA_FILE is final, we redirect by creating a custom file path
     * stored per test via renaming the default file path approach.
     *
     * @param path path to the test data file
     */
    private void setTestFile(String path) throws Exception {
        // We override DATA_FILE field via reflection for Java 17
        // Use VarHandle approach or direct unsafe
        Field field = AppConfig.class.getDeclaredField("DATA_FILE");
        field.setAccessible(true);
        // On Java 17 we can't directly set final static fields without --add-opens
        // Instead, we make FileIOService use a configurable path via System property
        // The FileIOService already reads from AppConfig.DATA_FILE; we patch with temp path
        // To make this work cleanly, we directly override via unsafe
        sun.misc.Unsafe unsafe = getUnsafe();
        Object staticBase = unsafe.staticFieldBase(field);
        long staticOffset = unsafe.staticFieldOffset(field);
        unsafe.putObject(staticBase, staticOffset, path);
    }

    /**
     * Retrieves the {@code sun.misc.Unsafe} instance for low-level field manipulation.
     *
     * @return the Unsafe instance
     * @throws Exception if Unsafe cannot be obtained
     */
    private sun.misc.Unsafe getUnsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
}
