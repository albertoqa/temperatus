package temperatus.model.dao;

import temperatus.model.pojo.Project;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface ProjectDao extends GenericDao {

    Project getByName(String name);
    int getIdByName(String name);
    List getAllProjectNames();

}
