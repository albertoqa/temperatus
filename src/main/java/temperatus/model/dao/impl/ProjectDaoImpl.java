package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.ProjectDao;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class ProjectDaoImpl extends GenericDaoImpl implements ProjectDao{

    @Override
    public int getIdByName(String name) {
        return (int) this.sessionFactory.getCurrentSession()
                .createSQLQuery(
                        "select PROJECT.ID from PROJECT where PROJECT.NAME = :name")
                .setParameter("name", name)
                .uniqueResult();
    }

    @Override
    public List<String> getAllProjectNames() {
        return this.sessionFactory.getCurrentSession()
                .createSQLQuery("select PROJECT.NAME from PROJECT")
                .list();
    }
}
