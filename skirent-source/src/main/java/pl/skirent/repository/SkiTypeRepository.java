package pl.skirent.repository;

import pl.skirent.model.SkiType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiTypeRepository {
    private final List<SkiType> skiTypes = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void add(SkiType skiType) {
        if (skiType.getId() == 0) {
            skiType.setId(nextId.getAndIncrement());
        } else {
            nextId.updateAndGet(n -> Math.max(n, skiType.getId() + 1));
        }
        skiTypes.add(skiType);
    }

    public void addWithId(SkiType skiType, int id) {
        skiType.setId(id);
        nextId.updateAndGet(n -> Math.max(n, id + 1));
        skiTypes.add(skiType);
    }

    public List<SkiType> findAll() { return new ArrayList<>(skiTypes); }

    public SkiType findById(int id) {
        return skiTypes.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    public void update(SkiType skiType) {
        for (int i = 0; i < skiTypes.size(); i++) {
            if (skiTypes.get(i).getId() == skiType.getId()) {
                skiTypes.set(i, skiType);
                return;
            }
        }
    }

    public void delete(int id) {
        skiTypes.removeIf(s -> s.getId() == id);
    }

    public void clear() {
        skiTypes.clear();
        nextId.set(1);
    }
}
