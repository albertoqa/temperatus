package temperatus.model.service;

import temperatus.model.pojo.Subject;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface SubjectService {

    Subject getById(int id);
    void save(Subject subject);
    void delete(Subject subject);
    List<Subject> getAll();
    void saveOrUpdate(Subject subject);

}
