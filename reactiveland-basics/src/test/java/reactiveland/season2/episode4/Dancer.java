package reactiveland.season2.episode4;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("Dancers")
public record Dancer(@Id String id,
                     @Nullable @Column("last_danced_at") LocalDateTime lastDancedAt,
                     @Column("dance_type_competency") Dancer.DanceType danceTypeCompetency)
        implements Persistable<String> {

    public static Dancer newDancer(DanceType danceType) {
        return new Dancer(UUID.randomUUID().toString(), LocalDate.of(2001, 1, 1).atStartOfDay(), danceType);
    }

    public Dancer dance(){
        return new Dancer(this.id, LocalDateTime.now(), this.danceTypeCompetency);
    }

    @Override
    public boolean isNew() {
        return lastDancedAt.isAfter(LocalDate.of(2000, 1, 1).atStartOfDay()) && !id.isBlank();
    }

    @Override
    public String getId() {
        return id;
    }

    public enum DanceType {
        FREE_STYLE, TECHNO, SALSA
    }
}
