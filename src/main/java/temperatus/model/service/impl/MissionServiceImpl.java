package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
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
    public void save(Mission mission) throws ControlledTemperatusException {
        missionDao.save(mission);
    }

    @Override
    public void delete(Mission mission) {
        missionDao.delete(mission);
    }

    @Override
    public List<Mission> getAll() {
        return missionDao.getAll(Mission.class);
    }

    @Override
    public void saveOrUpdate(Mission mission) throws ControlledTemperatusException{

        if(mission.getName().length() < 1 || mission.getName().length() > 100) {
            throw new ControlledTemperatusException("Invalid name length");
        } else if(mission.getGame() == null || mission.getAuthor() == null || mission.getProject() == null || mission.getSubject() == null) {
            throw new ControlledTemperatusException("Project, Game and Subject cannot be null");
        } else if(mission.getDateIni() == null){
            throw new ControlledTemperatusException("Date cannot be null");
        }

        // TODO translate and check

        missionDao.saveOrUpdate(mission);
    }

}
