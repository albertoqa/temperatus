package temperatus.model.service;

import temperatus.model.pojo.Experiment;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface ExperimentService {

    Experiment getById(int id);
    void save(Experiment experiment);
    void delete(Experiment experiment);
    List<Experiment> getAll();
    void saveOrUpdate(Experiment experiment);

    List<Integer> getExperimentNamesRelatedToProject(String project);

}
