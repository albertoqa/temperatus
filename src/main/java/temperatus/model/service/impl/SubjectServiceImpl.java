package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.dao.SubjectDao;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectDao subjectDao;

    @Override
    public Subject getById(int id) {
        return subjectDao.get(Subject.class, id);
    }

    @Override
    public void save(Subject subject) throws ControlledTemperatusException {

        if(subject.getName().length() < 1 || subject.getName().length() > 300) {
            throw new ControlledTemperatusException("Invalid name length");
        }

        subjectDao.save(subject);
    }

    @Override
    public void delete(Subject subject) {
        subjectDao.delete(subject);
    }

    @Override
    public List<Subject> getAll() {
        return subjectDao.getAll(Subject.class);
    }

    @Override
    public void saveOrUpdate(Subject subject) {
        subjectDao.saveOrUpdate(subject);
    }
}
