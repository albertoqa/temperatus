package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.ExperimentDao;
import temperatus.model.pojo.Experiment;
import temperatus.model.service.ExperimentService;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Service
@Transactional
public class ExperimentServiceImpl implements ExperimentService {

    @Autowired
    ExperimentDao experimentDao;

    @Override
    public Experiment getById(int id) {
        return experimentDao.get(Experiment.class, id);
    }

    @Override
    public void save(Experiment experiment) {
        experimentDao.save(experiment);
    }

    @Override
    public void delete(Experiment experiment) {
        experimentDao.delete(experiment);
    }

    @Override
    public List<Experiment> getAll() {
        return experimentDao.getAll(Experiment.class);
    }

    @Override
    public void saveOrUpdate(Experiment experiment) {
        experimentDao.saveOrUpdate(experiment);
    }

    @Override
    public List<Integer> getExperimentNamesRelatedToProject(String project) {
        return experimentDao.getExperimentNamesRelatedToProject(project);
    }
}
