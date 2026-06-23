package pl.skirent.io;

import pl.skirent.config.AppConfig;
import pl.skirent.model.Customer;
import pl.skirent.model.Rental;
import pl.skirent.model.Ski;
import pl.skirent.model.SkiType;
import pl.skirent.repository.CustomerRepository;
import pl.skirent.repository.RentalRepository;
import pl.skirent.repository.SkiRepository;
import pl.skirent.repository.SkiTypeRepository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FileIOService {

    private static volatile FileIOService instance;
    private final ExecutorService executorService;

    private FileIOService() {
        executorService = Executors.newFixedThreadPool(AppConfig.THREAD_POOL_SIZE);
    }

    public static synchronized FileIOService getInstance() {
        if (instance == null) {
            instance = new FileIOService();
        }
        return instance;
    }

    private String getDataFile() {
        String prop = System.getProperty("skirent.data.file");
        return prop != null ? prop : AppConfig.DATA_FILE;
    }

    public Future<?> saveAllData(SkiTypeRepository skiTypeRepo, SkiRepository skiRepo,
                            CustomerRepository customerRepo, RentalRepository rentalRepo) {
        List<SkiType> skiTypes = skiTypeRepo.findAll();
        List<Ski> skis = skiRepo.findAll();
        List<Customer> customers = customerRepo.findAll();
        List<Rental> rentals = rentalRepo.findAll();

        return executorService.submit(() -> {
            try (DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(getDataFile())))) {

                // SkiTypes
                dos.writeInt(skiTypes.size());
                for (SkiType st : skiTypes) {
                    dos.writeInt(st.getId());
                    dos.writeUTF(st.getName() == null ? "" : st.getName());
                    dos.writeUTF(st.getDescription() == null ? "" : st.getDescription());
                }

                // Skis
                dos.writeInt(skis.size());
                for (Ski s : skis) {
                    dos.writeInt(s.getId());
                    dos.writeInt(s.getSkiTypeId());
                    dos.writeUTF(s.getBrand() == null ? "" : s.getBrand());
                    dos.writeUTF(s.getModel() == null ? "" : s.getModel());
                    dos.writeUTF(s.getBindings() == null ? "" : s.getBindings());
                    dos.writeDouble(s.getLength());
                }

                // Customers
                dos.writeInt(customers.size());
                for (Customer c : customers) {
                    dos.writeInt(c.getId());
                    dos.writeUTF(c.getFirstName() == null ? "" : c.getFirstName());
                    dos.writeUTF(c.getLastName() == null ? "" : c.getLastName());
                    dos.writeUTF(c.getDocumentNumber() == null ? "" : c.getDocumentNumber());
                    dos.writeUTF(c.getDescription() == null ? "" : c.getDescription());
                }

                // Rentals
                dos.writeInt(rentals.size());
                for (Rental r : rentals) {
                    dos.writeInt(r.getId());
                    dos.writeInt(r.getCustomerId());
                    List<Integer> skiIds = r.getSkiIds() == null ? new ArrayList<>() : r.getSkiIds();
                    dos.writeInt(skiIds.size());
                    for (int skiId : skiIds) {
                        dos.writeInt(skiId);
                    }
                    dos.writeUTF(r.getFrom() == null ? "" : r.getFrom().toString());
                    dos.writeUTF(r.getTo() == null ? "" : r.getTo().toString());
                    dos.writeUTF(r.getStatus() == null ? "" : r.getStatus());
                    dos.writeUTF(r.getRemarks() == null ? "" : r.getRemarks());
                }

            } catch (IOException e) {
                System.err.println("Error saving data: " + e.getMessage());
            }
        });
    }

    public void loadAllData(SkiTypeRepository skiTypeRepo, SkiRepository skiRepo,
                            CustomerRepository customerRepo, RentalRepository rentalRepo) {
        File file = new File(getDataFile());
        if (!file.exists()) {
            return;
        }

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            // SkiTypes
            int skiTypeCount = dis.readInt();
            for (int i = 0; i < skiTypeCount; i++) {
                int id = dis.readInt();
                String name = dis.readUTF();
                String description = dis.readUTF();
                SkiType st = new SkiType(0, name, description);
                skiTypeRepo.addWithId(st, id);
            }

            // Skis
            int skiCount = dis.readInt();
            for (int i = 0; i < skiCount; i++) {
                int id = dis.readInt();
                int skiTypeId = dis.readInt();
                String brand = dis.readUTF();
                String model = dis.readUTF();
                String bindings = dis.readUTF();
                double length = dis.readDouble();
                Ski s = new Ski(0, skiTypeId, brand, model, bindings, length);
                skiRepo.addWithId(s, id);
            }

            // Customers
            int customerCount = dis.readInt();
            for (int i = 0; i < customerCount; i++) {
                int id = dis.readInt();
                String firstName = dis.readUTF();
                String lastName = dis.readUTF();
                String documentNumber = dis.readUTF();
                String description = dis.readUTF();
                Customer c = new Customer(0, firstName, lastName, documentNumber, description);
                customerRepo.addWithId(c, id);
            }

            // Rentals
            int rentalCount = dis.readInt();
            for (int i = 0; i < rentalCount; i++) {
                int id = dis.readInt();
                int customerId = dis.readInt();
                int skiIdCount = dis.readInt();
                List<Integer> skiIds = new ArrayList<>();
                for (int j = 0; j < skiIdCount; j++) {
                    skiIds.add(dis.readInt());
                }
                String fromStr = dis.readUTF();
                String toStr = dis.readUTF();
                String status = dis.readUTF();
                String remarks = dis.readUTF();
                LocalDateTime from = fromStr.isEmpty() ? null : LocalDateTime.parse(fromStr);
                LocalDateTime to = toStr.isEmpty() ? null : LocalDateTime.parse(toStr);
                Rental r = new Rental(0, customerId, skiIds, from, to, status, remarks);
                rentalRepo.addWithId(r, id);
            }

        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
