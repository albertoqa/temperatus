package temperatus.model.service;

import temperatus.model.pojo.Record;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface RecordService {

    Record getById(int id);

    void save(Record record);

    void delete(Record record);

    List<Record> getAll();

    void saveOrUpdate(Record record);

    List<Record> getByMissionId(int missionId);

}
