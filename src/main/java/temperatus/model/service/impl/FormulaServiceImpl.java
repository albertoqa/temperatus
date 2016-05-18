package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
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
    public void saveOrUpdate(Formula formula) throws ControlledTemperatusException {
        if(formula.getName() == null || formula.getName().length() < 1) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_FORMULA_NAME));
        } else if(formula.getOperation().isEmpty()) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.FORMULA_CANNOT_BE_EMPTY));
        }
        formulaDao.saveOrUpdate(formula);
    }

    @Override
    public Formula getByName(String name) {
        return formulaDao.getByName(name);
    }


}
