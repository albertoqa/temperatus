package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.ExperimentDao;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class ExperimentDaoImpl extends GenericDaoImpl implements ExperimentDao{


    @Override
    public List<Integer> getExperimentNamesRelatedToProject(String project) {
        return this.sessionFactory.getCurrentSession()
                .createSQLQuery(
                        "select EXPERIMENT.ID from EXPERIMENT INNER JOIN PROJECT ON PROJECT.ID = EXPERIMENT.PROJECT_ID AND PROJECT.NAME = :projectName")
                .setParameter("projectName", project)
                .list();
    }
}
