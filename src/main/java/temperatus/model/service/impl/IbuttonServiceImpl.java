package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.dao.IbuttonDao;
import temperatus.model.pojo.Ibutton;
import temperatus.model.service.IbuttonService;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
@Service
@Transactional
public class IbuttonServiceImpl implements IbuttonService {

    @Autowired
    private IbuttonDao ibuttonDao;

    @Override
    public Ibutton getById(int id) {
        return ibuttonDao.get(Ibutton.class, id);
    }

    @Override
    public void save(Ibutton ibutton) {
        ibuttonDao.save(ibutton);
    }

    @Override
    public void delete(Ibutton ibutton) {
        ibuttonDao.delete(ibutton);
    }

    @Override
    public List<Ibutton> getAll() {
        return ibuttonDao.getAll(Ibutton.class);
    }

    @Override
    public void saveOrUpdate(Ibutton ibutton) throws ControlledTemperatusException{
        if(ibutton.getAlias() == null || ibutton.getAlias().length() < 1 || ibutton.getAlias().length() > 100) {
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_DEVICE_NAME));
        }
        ibuttonDao.saveOrUpdate(ibutton);
    }

    @Override
    public Ibutton getBySerial(String serial) {
        return ibuttonDao.getBySerial(serial);
    }


}
