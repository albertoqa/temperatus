package temperatus.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import temperatus.controller.archived.ArchivedController;
import temperatus.controller.archived.MissionInfoController;
import temperatus.controller.button.DeviceGeneralInfoController;
import temperatus.controller.creation.*;
import temperatus.controller.manage.ManageController;
import temperatus.controller.manage.ManageSubjectController;

/**
 * Created by alberto on 17/1/16.
 */
@Configuration
@ImportResource("classpath:config/spring-config.xml")
public class SpringApplicationConfig {

    @Bean
    @Scope("prototype")
    public ArchivedController archivedController() {
        return new ArchivedController();
    }

    @Bean
    @Scope("prototype")
    public MissionInfoController missionInfoController() {
        return new MissionInfoController();
    }

    @Bean
    @Scope("prototype")
    public ManageSubjectController manageSubjectController() {
        return new ManageSubjectController();
    }

    @Bean
    @Scope("prototype")
    public DeviceGeneralInfoController deviceGeneralInfoController() {
        return new DeviceGeneralInfoController();
    }

    @Bean
    @Scope("prototype")
    public ManageController manageController() {
        return new ManageController();
    }

    @Bean
    @Scope("prototype")
    public NewGameController newGameController() {
        return new NewGameController();
    }

    @Bean
    @Scope("prototype")
    public NewIButtonController newIButtonController() {
        return new NewIButtonController();
    }

    @Bean
    @Scope("prototype")
    public NewMissionController newMissionController() {
        return new NewMissionController();
    }

    @Bean
    @Scope("prototype")
    public NewPositionController newPositionController() {
        return new NewPositionController();
    }

    @Bean
    @Scope("prototype")
    public NewProjectController newProjectController() {
        return new NewProjectController();
    }

    @Bean
    @Scope("prototype")
    public NewRecordController newRecordController() {
        return new NewRecordController();
    }

    @Bean
    @Scope("prototype")
    public NewSubjectController newSubjectController() {
        return new NewSubjectController();
    }

    @Bean
    @Scope("prototype")
    public RecordConfigController recordConfigController() {
        return new RecordConfigController();
    }

    @Bean
    @Scope("prototype")
    public RecordInfoPaneController recordInfoPaneController() {
        return new RecordInfoPaneController();
    }

}
