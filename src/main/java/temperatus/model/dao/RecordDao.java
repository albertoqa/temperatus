package temperatus.model.dao;

import temperatus.model.pojo.Record;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface RecordDao extends GenericDao {

    List<Record> getByMissionId(int missionId);

}
