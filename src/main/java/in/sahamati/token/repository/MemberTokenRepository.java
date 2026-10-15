package in.sahamati.token.repository;

import in.sahamati.token.entity.MemberToken;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class MemberTokenRepository implements PanacheRepository<MemberToken> {

    public Optional<MemberToken> findByMemberId(String memberId) {
        return find("memberId", memberId).firstResultOptional();
    }
}
