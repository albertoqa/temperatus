package temperatus.model.service;

import temperatus.model.pojo.Measurement;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface MeasurementService {

    Measurement getById(int id);
    void save(Measurement measurement);
    void delete(Measurement measurement);
    List<Measurement> getAll();
    void saveOrUpdate(Measurement measurement);

    List<Measurement> getAllByRecordId(int recordId);
}
