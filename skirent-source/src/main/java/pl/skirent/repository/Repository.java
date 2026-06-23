package pl.skirent.repository;

import java.util.List;

public interface Repository<T> {
    void add(T entity);
    void update(T entity);
    void delete(int id);
    T findById(int id);
    List<T> findAll();
}
