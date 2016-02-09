package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.dao.AuthorDao;
import temperatus.model.pojo.Author;
import temperatus.model.service.AuthorService;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    AuthorDao authorDao;

    @Override
    public Author getById(int id) {
        return authorDao.get(Author.class, id);
    }

    @Override
    public void save(Author author) throws ControlledTemperatusException {
        authorDao.save(author);
    }

    @Override
    public void delete(Author author) {
        authorDao.delete(author);
    }

    @Override
    public List<Author> getAll() {
        return authorDao.getAll(Author.class);
    }

    @Override
    public void saveOrUpdate(Author author) {
        authorDao.saveOrUpdate(author);
    }
}
