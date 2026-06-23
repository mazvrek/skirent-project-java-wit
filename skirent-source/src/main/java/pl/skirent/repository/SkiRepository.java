package pl.skirent.repository;

import pl.skirent.model.Ski;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiRepository {
    private final List<Ski> skis = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void add(Ski ski) {
        if (ski.getId() == 0) {
            ski.setId(nextId.getAndIncrement());
        } else {
            nextId.updateAndGet(n -> Math.max(n, ski.getId() + 1));
        }
        skis.add(ski);
    }

    public void addWithId(Ski ski, int id) {
        ski.setId(id);
        nextId.updateAndGet(n -> Math.max(n, id + 1));
        skis.add(ski);
    }

    public List<Ski> findAll() { return new ArrayList<>(skis); }

    public Ski findById(int id) {
        return skis.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    public void update(Ski ski) {
        for (int i = 0; i < skis.size(); i++) {
            if (skis.get(i).getId() == ski.getId()) {
                skis.set(i, ski);
                return;
            }
        }
    }

    public void delete(int id) {
        skis.removeIf(s -> s.getId() == id);
    }

    public void clear() {
        skis.clear();
        nextId.set(1);
    }
}
