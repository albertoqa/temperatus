package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.MeasurementDao;
import temperatus.model.pojo.Measurement;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class MeasurementDaoImpl extends GenericDaoImpl implements MeasurementDao {

    @Override
    public List<Measurement> getAllByRecordId(int recordId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from Measurement where recordId=:recordId")
                .setParameter("recordId", recordId)
                .list();
    }
}
