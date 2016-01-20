package temperatus.model.service;

import temperatus.model.pojo.Mision;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface MisionService {

    Mision getById(int id);
    void save(Mision Mision);
    void delete(Mision Mision);
    List<Mision> getAll();
    void saveOrUpdate(Mision Mision);

    List<Integer> getMisionNamesRelatedToProject(String project);

}
