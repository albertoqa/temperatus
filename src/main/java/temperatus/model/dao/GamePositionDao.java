package temperatus.model.dao;

import temperatus.model.pojo.GamePosition;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface GamePositionDao extends GenericDao {

    List<GamePosition> getAllForGame(int gameId);

}
