package pl.skirent.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.skirent.model.SkiType;
import pl.skirent.repository.SkiTypeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SkiTypeRepository}.
 * Tests CRUD operations: add, findById, findAll, delete, update.
 */
public class SkiTypeRepositoryTest {

    /** The repository under test, freshly created before each test. */
    private SkiTypeRepository repo;

    /**
     * Initialises a fresh empty repository before each test method.
     */
    @BeforeEach
    public void setUp() {
        repo = new SkiTypeRepository();
    }

    /**
     * Tests that adding a ski type assigns an auto-incremented id and stores the entity.
     */
    @Test
    public void testAdd() {
        SkiType st = new SkiType(0, "Alpine", "Downhill skiing");
        repo.add(st);
        assertEquals(1, st.getId(), "First added entity should have id=1");
        assertNotNull(repo.findById(1));
    }

    /**
     * Tests that findById returns the correct entity by its id.
     */
    @Test
    public void testFindById() {
        SkiType st = new SkiType(0, "Freestyle", "Tricks skiing");
        repo.add(st);
        SkiType found = repo.findById(st.getId());
        assertNotNull(found);
        assertEquals("Freestyle", found.getName());
    }

    /**
     * Tests that findAll returns all added entities.
     */
    @Test
    public void testFindAll() {
        repo.add(new SkiType(0, "Alpine", "A"));
        repo.add(new SkiType(0, "Nordic", "N"));
        List<SkiType> all = repo.findAll();
        assertEquals(2, all.size());
    }

    /**
     * Tests that delete removes the entity with the given id.
     */
    @Test
    public void testDelete() {
        SkiType st = new SkiType(0, "Telemark", "Old school");
        repo.add(st);
        int id = st.getId();
        repo.delete(id);
        assertNull(repo.findById(id), "Entity should be null after deletion");
        assertEquals(0, repo.findAll().size());
    }

    /**
     * Tests that update replaces the fields of an existing entity.
     */
    @Test
    public void testUpdate() {
        SkiType st = new SkiType(0, "Alpine", "Downhill");
        repo.add(st);
        st.setName("Updated Alpine");
        st.setDescription("Updated description");
        repo.update(st);
        SkiType updated = repo.findById(st.getId());
        assertEquals("Updated Alpine", updated.getName());
        assertEquals("Updated description", updated.getDescription());
    }

    /**
     * Tests that findById returns null for a non-existent id.
     */
    @Test
    public void testFindByIdNotFound() {
        assertNull(repo.findById(999));
    }
}
