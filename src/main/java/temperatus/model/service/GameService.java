package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Game;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface GameService {

    Game getById(int id);

    void save(Game game) throws ControlledTemperatusException;

    void delete(Game game);

    List<Game> getAll();

    void saveOrUpdate(Game game) throws ControlledTemperatusException;

}
