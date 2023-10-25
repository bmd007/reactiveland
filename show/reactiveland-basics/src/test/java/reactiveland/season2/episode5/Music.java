package reactiveland.season2.episode5;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Table("Musics")
public record Music(@Nullable @Column("singer_name") String singer_name,
                    @Id @Column("name") String name,
                    @Nullable @Column("released_at") LocalDateTime releasedAt) implements Persistable<String> {

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isNew() {
        return releasedAt.isAfter(LocalDateTime.now().minusYears(2000));
    }
}
