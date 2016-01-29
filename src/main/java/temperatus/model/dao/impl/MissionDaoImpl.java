package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.MissionDao;
import temperatus.model.pojo.Mission;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class MissionDaoImpl extends GenericDaoImpl implements MissionDao {

    @Override
    public List<Mission> getAllForProject(int projectId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from Mission where projectId=:pId")
                .setParameter("pId", projectId)
                .list();
    }

}
