package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Mission;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface MissionService {

    Mission getById(int id);

    void save(Mission mission) throws ControlledTemperatusException;

    void delete(Mission mission);

    List<Mission> getAll();

    void saveOrUpdate(Mission mission);

}
