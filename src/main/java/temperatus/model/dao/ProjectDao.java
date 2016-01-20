package temperatus.model.dao;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface ProjectDao extends GenericDao {

    int getIdByName(String name);
    List<String> getAllProjectNames();

}
