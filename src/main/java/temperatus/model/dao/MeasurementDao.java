package temperatus.model.dao;

import temperatus.model.pojo.Measurement;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface MeasurementDao extends GenericDao {

    List<Measurement> getAllByRecordId(int recordId);

}
