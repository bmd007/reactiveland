package reactiveland.season2.episode5;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactiveland.season2.episode4.Dancer;

@Repository
public interface MusicRepository extends ReactiveCrudRepository<Music, String> {
}
