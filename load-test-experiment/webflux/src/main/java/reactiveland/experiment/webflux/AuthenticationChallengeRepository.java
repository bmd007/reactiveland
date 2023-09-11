package reactiveland.experiment.webflux;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationChallengeRepository extends R2dbcRepository<AuthenticationChallenge, String> {
}
