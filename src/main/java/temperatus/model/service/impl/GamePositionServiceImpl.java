package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.GamePositionDao;
import temperatus.model.pojo.GamePosition;
import temperatus.model.service.GamePositionService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class GamePositionServiceImpl implements GamePositionService {

    @Autowired
    private GamePositionDao gamePositionDao;

    @Override
    public GamePosition getById(int id) {
        return gamePositionDao.get(GamePosition.class, id);
    }

    @Override
    public void save(GamePosition gamePosition) {
        gamePositionDao.save(gamePosition);
    }

    @Override
    public void delete(GamePosition gamePosition) {
        gamePositionDao.delete(gamePosition);
    }

    @Override
    public List<GamePosition> getAll() {
        return gamePositionDao.getAll(GamePosition.class);
    }

    @Override
    public void saveOrUpdate(GamePosition gamePosition) {
        gamePositionDao.saveOrUpdate(gamePosition);
    }

    @Override
    public List<GamePosition> getAllForGame(int gameId) {
        return gamePositionDao.getAllForGame(gameId);
    }

}
