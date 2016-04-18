package temperatus.model.dao;

import temperatus.model.pojo.Ibutton;

/**
 * Created by alberto on 26/12/15.
 */
public interface IbuttonDao extends GenericDao {

    Ibutton getBySerial(String serial);

}
