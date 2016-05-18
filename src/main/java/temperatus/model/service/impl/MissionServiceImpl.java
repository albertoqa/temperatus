package temperatus.model.service.impl;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.dao.MissionDao;
import temperatus.model.pojo.Mission;
import temperatus.model.service.MissionService;

import java.io.File;
import java.io.IOException;
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
        if(mission.getRecords().size() > 0) {
            try {
                FileUtils.deleteDirectory(new File(mission.getRecords().iterator().next().getDataPath()).getParentFile());
            } catch (IOException e) {
                LoggerFactory.getLogger(MissionServiceImpl.class.getName()).warn("Cannot delete mission files");
            }
        }
        missionDao.delete(mission);
    }

    @Override
    public List<Mission> getAll() {
        return missionDao.getAll(Mission.class);
    }

    @Override
    public void saveOrUpdate(Mission mission) throws ControlledTemperatusException{
        if(mission.getName() == null || mission.getName().length() < 1) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_MISSION_NAME));
        }
        missionDao.saveOrUpdate(mission);
    }

}
