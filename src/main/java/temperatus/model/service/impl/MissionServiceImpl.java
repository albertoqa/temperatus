package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.MissionDao;
import temperatus.model.pojo.Mission;
import temperatus.model.service.MissionService;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Service
@Transactional
public class MissionServiceImpl implements MissionService {

    @Autowired
    private MissionDao missionDao;

    @Override
    public Mission getById(int id) {
        return missionDao.get(Mission.class, id);
    }

    @Override
    public void save(Mission Mission) {
        missionDao.save(Mission);
    }

    @Override
    public void delete(Mission Mission) {
        missionDao.delete(Mission);
    }

    @Override
    public List<Mission> getAll() {
        return missionDao.getAll(Mission.class);
    }

    @Override
    public void saveOrUpdate(Mission Mission) {
        missionDao.saveOrUpdate(Mission);
    }

    @Override
    public List<Mission> getAllForProject(int projectId) {
        return missionDao.getAllForProject(projectId);
    }


}
