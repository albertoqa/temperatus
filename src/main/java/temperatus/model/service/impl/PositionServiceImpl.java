package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
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

    private final PositionDao positionDao;

    @Autowired
    public PositionServiceImpl(PositionDao positionDao) {
        this.positionDao = positionDao;
    }

    @Override
    public Position getById(int id) {
        return positionDao.get(Position.class, id);
    }

    @Override
    public void save(Position position) throws ControlledTemperatusException {
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
    public void saveOrUpdate(Position position) throws ControlledTemperatusException {
        if (position.getPlace() == null || position.getPlace().length() < 1) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_POSITION_NAME));
        }
        positionDao.saveOrUpdate(position);
    }
}
