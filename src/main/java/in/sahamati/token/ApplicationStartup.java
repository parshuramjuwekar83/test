package in.sahamati.token;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ApplicationStartup {
    private static final Logger LOG = Logger.getLogger(ApplicationStartup.class);

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Sahamati Token Service starting...");
    }
}
