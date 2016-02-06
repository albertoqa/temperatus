package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.dao.GameDao;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class GameServiceImpl implements GameService {

    @Autowired
    private GameDao gameDao;

    @Override
    public Game getById(int id) {
        return gameDao.get(Game.class, id);
    }

    @Override
    public void save(Game game) throws ControlledTemperatusException {

        if(game.getTitle().length() < 1) {
            throw new ControlledTemperatusException("Name cannot be empty");
        } else if(game.getTitle().length() > 100) {
            throw new ControlledTemperatusException("Name cannot be longer than 100");
        } else if(game.getNumButtons() > 30 || game.getNumButtons() < 1) {
            throw new ControlledTemperatusException("Invalid number of iButtons");
        }

        gameDao.save(game);
    }

    @Override
    public void delete(Game game) {
        gameDao.delete(game);
    }

    @Override
    public List<Game> getAll() {
        return gameDao.getAll(Game.class);
    }

    @Override
    public void saveOrUpdate(Game game) {
        gameDao.saveOrUpdate(game);
    }
}
