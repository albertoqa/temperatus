package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.RecordDao;
import temperatus.model.pojo.Record;
import temperatus.model.service.RecordService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordDao recordDao;

    @Override
    public Record getById(int id) {
        return recordDao.get(Record.class, id);
    }

    @Override
    public void save(Record record) {
        recordDao.save(record);
    }

    @Override
    public void delete(Record record) {
        recordDao.delete(record);
    }

    @Override
    public List<Record> getAll() {
        return recordDao.getAll(Record.class);
    }

    @Override
    public void saveOrUpdate(Record record) {
        recordDao.saveOrUpdate(record);
    }
}
