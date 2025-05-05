package me.suhsaechan.suhgrassplanter.repository;

import java.util.UUID;
import me.suhsaechan.suhgrassplanter.model.postgres.CommitLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitLogRepository extends JpaRepository<CommitLog, UUID> {

}
