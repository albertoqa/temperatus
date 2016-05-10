package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.dao.AuthorDao;
import temperatus.model.pojo.Author;
import temperatus.model.service.AuthorService;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
@Service
@Transactional
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    AuthorDao authorDao;

    @Override
    public Author getById(int id) {
        return authorDao.get(Author.class, id);
    }

    @Override
    public void save(Author author) {
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
    public void saveOrUpdate(Author author) throws ControlledTemperatusException {

        if(author.getName() == null || author.getName().length() < 1 || author.getName().length() > 100) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_AUTHOR_NAME));
        }

        authorDao.saveOrUpdate(author);
    }
}
