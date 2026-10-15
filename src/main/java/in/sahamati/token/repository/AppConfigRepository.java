package in.sahamati.token.repository;

import in.sahamati.token.entity.AppConfig;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AppConfigRepository implements PanacheRepository<AppConfig> {

    public Optional<AppConfig> findByIdKey(String id) {
        return find("id", id).firstResultOptional();
    }
}
