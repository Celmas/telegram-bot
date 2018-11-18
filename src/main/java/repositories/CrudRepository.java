package repositories;

public interface CrudRepository<T> {
    T find(Long id);
    void save(T model);
    void delete(Long id);
    void update(T model);
}
