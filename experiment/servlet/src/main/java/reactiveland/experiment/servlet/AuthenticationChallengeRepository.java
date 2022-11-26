package reactiveland.experiment.servlet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationChallengeRepository extends JpaRepository<AuthenticationChallenge, String> {
}
