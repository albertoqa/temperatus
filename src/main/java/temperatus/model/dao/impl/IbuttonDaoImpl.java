package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.IbuttonDao;
import temperatus.model.pojo.Ibutton;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class IbuttonDaoImpl extends GenericDaoImpl implements IbuttonDao{

    public IbuttonDaoImpl() {

    }

    @Override
    public Ibutton getBySerial(String serial) {
        return (Ibutton) this.sessionFactory.getCurrentSession()
                .createQuery("from Ibutton where serial=:serial")
                .setParameter("serial", serial)
                .uniqueResult();
    }

}
