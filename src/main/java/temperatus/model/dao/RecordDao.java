package temperatus.model.dao;

import temperatus.model.pojo.Record;

/**
 * Created by alberto on 26/12/15.
 */
public interface RecordDao extends GenericDao {

    Record getByMissionId(int missionId);

}
