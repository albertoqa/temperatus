package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.MeasurementDao;
import temperatus.model.pojo.Measurement;
import temperatus.model.service.MeasurementService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class MeasurementServiceImpl implements MeasurementService {

    @Autowired
    private MeasurementDao measurementDao;

    @Override
    public Measurement getById(int id) {
        return measurementDao.get(Measurement.class, id);
    }

    @Override
    public void save(Measurement measurement) {
        measurementDao.save(measurement);
    }

    @Override
    public void delete(Measurement measurement) {
        measurementDao.delete(measurement);
    }

    @Override
    public List<Measurement> getAll() {
        return measurementDao.getAll(Measurement.class);
    }

    @Override
    public void saveOrUpdate(Measurement measurement) {
        measurementDao.saveOrUpdate(measurement);
    }

    @Override
    public List<Measurement> getAllByRecordId(int recordId) {
        return measurementDao.getAllByRecordId(recordId);
    }
}
