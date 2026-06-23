package pl.skirent.repository;

import pl.skirent.model.Customer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomerRepository {
    private final List<Customer> customers = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void add(Customer customer) {
        if (customer.getId() == 0) {
            customer.setId(nextId.getAndIncrement());
        } else {
            nextId.updateAndGet(n -> Math.max(n, customer.getId() + 1));
        }
        customers.add(customer);
    }

    public void addWithId(Customer customer, int id) {
        customer.setId(id);
        nextId.updateAndGet(n -> Math.max(n, id + 1));
        customers.add(customer);
    }

    public List<Customer> findAll() { return new ArrayList<>(customers); }

    public Customer findById(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public void update(Customer customer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == customer.getId()) {
                customers.set(i, customer);
                return;
            }
        }
    }

    public void delete(int id) {
        customers.removeIf(c -> c.getId() == id);
    }

    public void clear() {
        customers.clear();
        nextId.set(1);
    }
}
