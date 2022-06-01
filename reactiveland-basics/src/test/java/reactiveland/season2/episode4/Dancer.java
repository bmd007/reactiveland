package reactiveland.season2.episode4;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Table("Dancers")
public record Dancer(@Id String id,
                     @Nullable @Column("last_danced_at") ZonedDateTime lastDancedAt,
                     @Column("dance_type_competency") Dancer.DanceType danceTypeCompetency)
        implements Persistable<String> {

    public static Dancer newDancer(DanceType danceType) {
        return new Dancer(UUID.randomUUID().toString(), LocalDateTime.MIN.atZone(ZoneId.systemDefault()), danceType);
    }

    @Override
    public boolean isNew() {
        return lastDancedAt.equals(LocalDateTime.MIN.atZone(ZoneId.systemDefault())) && !id.isBlank();
    }

    @Override
    public String getId() {
        return id;
    }

    public enum DanceType {
        FREE_STYLE, TECHNO, SALSA
    }
}
