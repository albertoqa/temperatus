package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.RecordDao;
import temperatus.model.pojo.Record;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class RecordDaoImpl extends GenericDaoImpl implements RecordDao {

    @Override
    public List<Record> getByMissionId(int missionId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery(
                        "from Record where Mission.id = :missionId")
                .setParameter("missionId", missionId)
                .list();
    }

}
