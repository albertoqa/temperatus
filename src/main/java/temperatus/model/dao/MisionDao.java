package temperatus.model.dao;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
public interface MisionDao extends GenericDao{

    List<Integer> getMisionNamesRelatedToProject(String project);


}
