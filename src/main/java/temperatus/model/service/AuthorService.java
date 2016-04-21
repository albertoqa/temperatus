package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Author;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
public interface AuthorService {

    Author getById(int id);

    void save(Author author);

    void delete(Author author);

    List<Author> getAll();

    void saveOrUpdate(Author author) throws ControlledTemperatusException;
}
