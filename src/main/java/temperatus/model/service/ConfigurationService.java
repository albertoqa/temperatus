package temperatus.model.service;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Configuration;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
public interface ConfigurationService {

    Configuration getById(int id);

    void save(Configuration configuration) throws ControlledTemperatusException;

    void delete(Configuration configuration);

    List<Configuration> getAll();

    void saveOrUpdate(Configuration configuration) throws ControlledTemperatusException;
}
