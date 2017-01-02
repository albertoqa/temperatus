package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Project;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface ProjectService {

    Project getById(int id);

    void save(Project project) throws ControlledTemperatusException;

    void delete(Project project);

    List<Project> getAll();

    void saveOrUpdate(Project project) throws ControlledTemperatusException;

    Project getByName(String name);

    int getIdByName(String name);

    List getAllProjectNames();

}
