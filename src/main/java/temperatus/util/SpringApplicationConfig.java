package temperatus.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Not necessary to define beans, component scan on config
 * <p>
 * Created by alberto on 17/1/16.
 */
@Configuration
@ImportResource("classpath:config/spring-config.xml")
public class SpringApplicationConfig {


}
