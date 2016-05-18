package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.dao.GameDao;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        gameDao.save(game);
    }

    @Override
    public void delete(Game game) {
        gameDao.delete(game);
    }

    @Override
    public List<Game> getAll() {
        List<Game> al = gameDao.getAll(Game.class);
        Set<Game> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);

        return al;
    }

    @Override
    public void saveOrUpdate(Game game) throws ControlledTemperatusException {
        if(game.getTitle() == null || game.getTitle().length() < 1) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_GAME_NAME));
        }
        gameDao.saveOrUpdate(game);
    }

}
