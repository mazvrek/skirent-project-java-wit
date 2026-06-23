package pl.skirent.config;

/**
 * Application-wide configuration constants.
 * All fields are static final and define global settings used across the application.
 */
public class AppConfig {

    /** Size of the thread pool used by {@link pl.skirent.io.FileIOService}. */
    public static final int THREAD_POOL_SIZE = 2;

    /** Path to the binary data file where application data is persisted. */
    public static final String DATA_FILE = "skirent_data.dat";

    /** Default language code for the user interface. */
    public static final String DEFAULT_LANGUAGE = "pl";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AppConfig() {
    }
}
