package temperatus.model.service;

import temperatus.model.pojo.Ibutton;

import java.util.List;

/**
 * Created by alberto on 24/1/16.
 */
public interface IbuttonService {

    Ibutton getById(int id);

    void save(Ibutton ibutton);

    void delete(Ibutton ibutton);

    List<Ibutton> getAll();

    void saveOrUpdate(Ibutton ibutton);

    Ibutton getBySerial(String serial);

}
