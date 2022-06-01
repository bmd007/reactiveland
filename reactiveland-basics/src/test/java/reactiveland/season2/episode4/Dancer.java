package reactiveland.season2.episode4;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
public record Dancer(@Id @NotBlank String id,
                     @Nullable LocalDateTime lastDancedAt,
                     @NotNull Dancer.DanceType competentAt)
        implements Persistable<String> {

    public static Dancer newDancer(DanceType danceType){
        return new Dancer(UUID.randomUUID().toString(), LocalDateTime.MIN, danceType);
    }

    @Override
    public boolean isNew() {
        return lastDancedAt.equals(LocalDateTime.MIN) && !id.isBlank();
    }

    @Override
    public String getId(){
        return id;
    }

    public enum DanceType {
        FREE_STYLE, TECHNO, SALSA
    }
}
