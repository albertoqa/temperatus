package temperatus.model.dao;

import temperatus.model.pojo.Mission;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface MissionDao extends GenericDao{

    List<Mission> getAllForProject(int projectId);

}
