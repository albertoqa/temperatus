package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.GamePositionDao;
import temperatus.model.pojo.GamePosition;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class GamePositionDaoImpl extends GenericDaoImpl implements GamePositionDao{

    public GamePositionDaoImpl() {
    }

    @Override
    public List<GamePosition> getAllForGame(int gameId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from GamePosition where gameId=:gameId")
                .setParameter("gameId", gameId)
                .list();
    }
}
