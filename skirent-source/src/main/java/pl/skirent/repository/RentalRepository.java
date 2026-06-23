package pl.skirent.repository;

import pl.skirent.model.Rental;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RentalRepository {
    private final List<Rental> rentals = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void add(Rental rental) {
        if (rental.getId() == 0) {
            rental.setId(nextId.getAndIncrement());
        } else {
            nextId.updateAndGet(n -> Math.max(n, rental.getId() + 1));
        }
        rentals.add(rental);
    }

    public void addWithId(Rental rental, int id) {
        rental.setId(id);
        nextId.updateAndGet(n -> Math.max(n, id + 1));
        rentals.add(rental);
    }

    public List<Rental> findAll() { return new ArrayList<>(rentals); }

    public Rental findById(int id) {
        return rentals.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    public void update(Rental rental) {
        for (int i = 0; i < rentals.size(); i++) {
            if (rentals.get(i).getId() == rental.getId()) {
                rentals.set(i, rental);
                return;
            }
        }
    }

    public void delete(int id) {
        rentals.removeIf(r -> r.getId() == id);
    }

    public void clear() {
        rentals.clear();
        nextId.set(1);
    }
}
