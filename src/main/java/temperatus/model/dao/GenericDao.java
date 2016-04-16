package temperatus.model.dao;

import java.util.List;

/**
 * Generic methods that all DAOs will use and prevent the necessity
 * of implement them one time for each entity
 * <p>
 * This is a generic DAO interface which includes the most fundamental DAO
 * operations for any persistent object.
 * <p>
 * Created by alberto on 26/12/15.
 */
public interface GenericDao {

    <T> T save(final T o);
    void delete(final Object object);
    <T> T get(final Class<T> type, final int id);
    <T> T merge(final T o);
    <T> void saveOrUpdate(final T o);
    <T> List<T> getAll(final Class<T> type);

}