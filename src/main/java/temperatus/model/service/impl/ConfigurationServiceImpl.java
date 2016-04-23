package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.dao.ConfigurationDao;
import temperatus.model.pojo.Configuration;
import temperatus.model.service.ConfigurationService;

import java.util.List;

/**
 * Created by alberto on 9/2/16.
 */
@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    ConfigurationDao configurationDao;

    @Override
    public Configuration getById(int id) {
        return configurationDao.get(Configuration.class, id);
    }

    @Override
    public void save(Configuration configuration) throws ControlledTemperatusException {
        configurationDao.save(configuration);
    }

    @Override
    public void delete(Configuration configuration) {
        configurationDao.delete(configuration);
    }

    @Override
    public List<Configuration> getAll() {
        return configurationDao.getAll(Configuration.class);
    }

    @Override
    public void saveOrUpdate(Configuration configuration) throws ControlledTemperatusException {

        // TODO check constraints

        configurationDao.saveOrUpdate(configuration);
    }
}
