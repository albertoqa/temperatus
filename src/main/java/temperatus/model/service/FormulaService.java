package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Formula;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
public interface FormulaService {

    Formula getById(int id);

    void save(Formula formula) throws ControlledTemperatusException;

    void delete(Formula formula);

    List<Formula> getAll();

    void saveOrUpdate(Formula formula);

}
