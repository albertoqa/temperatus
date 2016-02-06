package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.dao.ProjectDao;
import temperatus.model.pojo.Project;
import temperatus.model.service.ProjectService;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    @Override
    public Project getById(int id) {
        return projectDao.get(Project.class, id);
    }

    @Override
    public void save(Project project) throws ControlledTemperatusException {

        if(project.getName().length() < 1) {
            throw new ControlledTemperatusException("Name cannot be empty");
        } else if(project.getName().length() > 100) {
            throw new ControlledTemperatusException("Name cannot be longer than 100");
        }

        projectDao.save(project);
    }

    @Override
    public void delete(Project project) {
        projectDao.delete(project);
    }

    @Override
    public List<Project> getAll() {
        return projectDao.getAll(Project.class);
    }

    @Override
    public void saveOrUpdate(Project project) {
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
    public List<String> getAllProjectNames() {
        return projectDao.getAllProjectNames();
    }

}
