package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.PositionDao;
import temperatus.model.pojo.Position;
import temperatus.model.service.PositionService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionDao positionDao;

    @Override
    public Position getById(int id) {
        return positionDao.get(Position.class, id);
    }

    @Override
    public void save(Position position) {
        positionDao.save(position);
    }

    @Override
    public void delete(Position position) {
        positionDao.delete(position);
    }

    @Override
    public List<Position> getAll() {
        return positionDao.getAll(Position.class);
    }

    @Override
    public void saveOrUpdate(Position position) {
        positionDao.saveOrUpdate(position);
    }
}
