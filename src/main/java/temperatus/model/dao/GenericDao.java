package temperatus.model.dao;

import java.util.List;

/**
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