package temperatus.model.service;

import temperatus.model.pojo.GamePosition;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface GamePositionService {

    GamePosition getById(int id);
    void save(GamePosition gamePosition);
    void delete(GamePosition gamePosition);
    List<GamePosition> getAll();
    void saveOrUpdate(GamePosition gamePosition);

}
