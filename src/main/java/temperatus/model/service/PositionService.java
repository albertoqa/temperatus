package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Position;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface PositionService {

    Position getById(int id);

    void save(Position position) throws ControlledTemperatusException;

    void delete(Position position);

    List<Position> getAll();

    void saveOrUpdate(Position position);

}
