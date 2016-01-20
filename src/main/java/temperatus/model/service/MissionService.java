package temperatus.model.service;

import temperatus.model.pojo.Mission;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface MissionService {

    Mission getById(int id);
    void save(Mission Mission);
    void delete(Mission Mission);
    List<Mission> getAll();
    void saveOrUpdate(Mission Mission);

}
