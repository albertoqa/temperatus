package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.RecordDao;
import temperatus.model.pojo.Record;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class RecordDaoImpl extends GenericDaoImpl implements RecordDao {

    @Override
    public Record getByMissionId(int missionId) {
        return (Record) this.sessionFactory.getCurrentSession()
                .createQuery(
                        "from Record where misionId = :missionId")
                .setParameter("missionId", missionId)
                .uniqueResult();
    }

}
