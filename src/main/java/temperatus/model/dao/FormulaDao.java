package temperatus.model.dao;

import temperatus.model.pojo.Formula;

/**
 * Created by alberto on 9/2/16.
 */
public interface FormulaDao extends GenericDao {

    Formula getByName(String name);

}
