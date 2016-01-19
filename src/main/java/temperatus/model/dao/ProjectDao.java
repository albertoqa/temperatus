package temperatus.model.dao;

import java.util.Date;
import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface ProjectDao extends GenericDao {

    Date getStartDate();
    Date getLastDate();
    List<String> getAuthorsInvolved(int id);
    int getIdByName(String name); // TODO es name unico??
    List<String> getAllProjectNames();

}
