package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.dao.FormulaDao;
import temperatus.model.pojo.Formula;
import temperatus.model.service.FormulaService;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
@Service
@Transactional
public class FormulaServiceImpl implements FormulaService {
    
    @Autowired
    FormulaDao formulaDao;

    @Override
    public Formula getById(int id) {
        return formulaDao.get(Formula.class, id);
    }

    @Override
    public void save(Formula formula) throws ControlledTemperatusException {
        formulaDao.save(formula);
    }

    @Override
    public void delete(Formula formula) {
        formulaDao.delete(formula);
    }

    @Override
    public List<Formula> getAll() {
        return formulaDao.getAll(Formula.class);
    }

    @Override
    public void saveOrUpdate(Formula formula) {
        formulaDao.saveOrUpdate(formula);
    }
    
}