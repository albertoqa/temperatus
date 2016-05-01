package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.types.Device;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show the general info of the device... all devices implement this view
 * <p>
 * Created by alberto on 13/2/16.
 */
@Controller
@Scope("prototype")
public class DeviceGeneralInfoController implements Initializable, AbstractController {

    @FXML private Label serialLabel;
    @FXML private Label modelLabel;
    @FXML private Label alternateNamesLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label defaultPosLabel;
    @FXML private Label aliasLabel;

    @FXML private Label serial;
    @FXML private Label model;
    @FXML private Label alternateNames;
    @FXML private Label description;
    @FXML private Label defaultPos;
    @FXML private Label alias;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    /**
     * Set device to show its info
     * @param device device selected
     */
    void setDevice(Device device) {
        setInfo(device.getSerial(), device.getModel(), device.getContainer().getAlternateNames(), device.getContainer().getDescription(), device.getDefaultPosition(), device.getAlias());
    }

    /**
     * Load device info on the view
     * @param serial device's serial
     * @param model device's model
     * @param altNames device's alternate names
     * @param desc device's description
     * @param pos device's default position (if any)
     * @param alias device's alias (if any)
     */
    private void setInfo(String serial, String model, String altNames, String desc, String pos, String alias) {
        this.serial.setText(serial);
        this.model.setText(model);
        this.alternateNames.setText(altNames);
        this.description.setText(desc);
        this.defaultPos.setText(pos);
        this.alias.setText(alias);
    }

    @Override
    public void translate() {
        serialLabel.setText(language.get(Lang.SERIAL_LABEL));
        modelLabel.setText(language.get(Lang.MODEL_LABEL));
        alternateNamesLabel.setText(language.get(Lang.ALTERNATE_NAMES_LABEL));
        descriptionLabel.setText(language.get(Lang.DESCRIPTION_LABEL));
        defaultPosLabel.setText(language.get(Lang.DEFAULT_POSITION_LABEL));
        aliasLabel.setText(language.get(Lang.ALIAS_LABEL));
    }
}
