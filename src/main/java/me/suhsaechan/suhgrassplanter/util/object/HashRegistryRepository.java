package me.suhsaechan.suhgrassplanter.util.object;

import java.util.Optional;
import java.util.UUID;
import me.suhsaechan.suhgrassplanter.model.constants.HashType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashRegistryRepository extends JpaRepository<HashRegistry, UUID> {
  Optional<HashRegistry> findByHashType(HashType hashType);
}
