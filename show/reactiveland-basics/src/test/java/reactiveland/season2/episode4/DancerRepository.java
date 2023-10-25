package reactiveland.season2.episode4;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DancerRepository extends ReactiveCrudRepository<Dancer, String> {
}
