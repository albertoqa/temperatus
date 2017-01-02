package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.ProjectDao;
import temperatus.model.pojo.Project;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class ProjectDaoImpl extends GenericDaoImpl implements ProjectDao{

    @Override
    public Project getByName(String name) {
        return (Project) this.sessionFactory.getCurrentSession()
                .createQuery("from Project where name=:name")
                .setParameter("name", name)
                .uniqueResult();
    }

    @Override
    public int getIdByName(String name) {
        return (int) this.sessionFactory.getCurrentSession()
                .createSQLQuery(
                        "select PROJECT.ID from PROJECT where PROJECT.NAME = :name")
                .setParameter("name", name)
                .uniqueResult();
    }

    @Override
    public List getAllProjectNames() {
        return this.sessionFactory.getCurrentSession()
                .createSQLQuery("select PROJECT.NAME from PROJECT")
                .list();
    }
}
