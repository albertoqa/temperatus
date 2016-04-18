package temperatus.model.dao.impl;

import org.springframework.stereotype.Repository;
import temperatus.model.dao.FormulaDao;
import temperatus.model.pojo.Formula;

/**
 * Created by alberto on 9/2/16.
 */
@Repository
public class FormulaDaoImpl extends GenericDaoImpl implements FormulaDao {

    @Override
    public Formula getByName(String name) {
        return (Formula) this.sessionFactory.getCurrentSession()
                .createQuery("from Formula where name=:name")
                .setParameter("name", name)
                .uniqueResult();
    }

}
