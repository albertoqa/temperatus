package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Subject;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface SubjectService {

    Subject getById(int id);

    void save(Subject subject) throws ControlledTemperatusException;

    void delete(Subject subject);

    List<Subject> getAll();

    void saveOrUpdate(Subject subject) throws ControlledTemperatusException;

}
