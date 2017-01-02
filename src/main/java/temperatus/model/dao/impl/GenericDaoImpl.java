package temperatus.model.dao.impl;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.pojo.Record;
import temperatus.util.User;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Repository
public class GenericDaoImpl {

    private static Logger history = LoggerFactory.getLogger("HISTORY");    // write the history of use of the application to a file

    @Autowired protected SessionFactory sessionFactory;

    public <T> T save(final T o) {
        return (T) sessionFactory.getCurrentSession().save(o);
    }

    public void delete(final Object object) {
        sessionFactory.getCurrentSession().delete(object);

        if (!(object instanceof Record)) {
            history.info(User.getUserName() + " " + Language.getInstance().get(Lang.DELETE_HISTORY) + " " + object.toString());
        }
    }

    public <T> T get(final Class<T> type, final int id) {
        return (T) sessionFactory.getCurrentSession().get(type, id);
    }

    public <T> T merge(final T o) {
        return (T) sessionFactory.getCurrentSession().merge(o);
    }

    public <T> void saveOrUpdate(final T o) {
        sessionFactory.getCurrentSession().saveOrUpdate(o);

        if (!(o instanceof Record)) {
            history.info(User.getUserName() + " " + Language.getInstance().get(Lang.SAVE_EDIT_HISTORY) + " " + o.toString());
        }
    }

    public <T> List getAll(final Class<T> type) {
        return sessionFactory.getCurrentSession().createCriteria(type).list();
    }

}
