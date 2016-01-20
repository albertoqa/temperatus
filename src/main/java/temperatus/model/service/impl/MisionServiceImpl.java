package temperatus.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temperatus.model.dao.MisionDao;
import temperatus.model.pojo.Mision;
import temperatus.model.service.MisionService;

import java.util.List;

/**
 * Created by alberto on 26/12/15.
 */
@Service
@Transactional
public class MisionServiceImpl implements MisionService {

    @Autowired
    MisionDao misionDao;

    @Override
    public Mision getById(int id) {
        return misionDao.get(Mision.class, id);
    }

    @Override
    public void save(Mision Mision) {
        misionDao.save(Mision);
    }

    @Override
    public void delete(Mision Mision) {
        misionDao.delete(Mision);
    }

    @Override
    public List<Mision> getAll() {
        return misionDao.getAll(Mision.class);
    }

    @Override
    public void saveOrUpdate(Mision Mision) {
        misionDao.saveOrUpdate(Mision);
    }

    @Override
    public List<Integer> getMisionNamesRelatedToProject(String project) {
        return misionDao.getMisionNamesRelatedToProject(project);
    }
}
