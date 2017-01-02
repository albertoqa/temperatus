package temperatus.model.service.impl;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.dao.ProjectDao;
import temperatus.model.pojo.Project;
import temperatus.model.service.ProjectService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDao projectDao;

    @Autowired
    public ProjectServiceImpl(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Override
    public Project getById(int id) {
        return projectDao.get(Project.class, id);
    }

    @Override
    public void save(Project project) throws ControlledTemperatusException {
        projectDao.save(project);
    }

    @Override
    public void delete(Project project) {
        project.getMissions().stream().filter(mission -> mission.getRecords().size() > 0).forEach(mission -> {
            try {
                FileUtils.deleteDirectory(new File(mission.getRecords().iterator().next().getDataPath()).getParentFile());
            } catch (IOException e) {
                LoggerFactory.getLogger(ProjectServiceImpl.class.getName()).warn("Cannot delete mission files");
            }
        });
        projectDao.delete(project);
    }

    @Override
    public List<Project> getAll() {
        return projectDao.getAll(Project.class);
    }

    @Override
    public void saveOrUpdate(Project project) throws ControlledTemperatusException {
        if (project.getName() == null || project.getName().length() < 1) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_PROJECT_NAME));
        }
        projectDao.saveOrUpdate(project);
    }

    @Override
    public Project getByName(String name) {
        return projectDao.getByName(name);
    }

    @Override
    public int getIdByName(String name) {
        return projectDao.getIdByName(name);
    }

    @Override
    public List getAllProjectNames() {
        return projectDao.getAllProjectNames();
    }

}
