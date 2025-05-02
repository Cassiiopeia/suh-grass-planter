package me.suhsaechan.suhgrassplanter.repository;

import java.util.Optional;
import java.util.UUID;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {
  Boolean existsByEmail(String email);

  Boolean existsByUsername(String username);
  Boolean existsByNickname(String nickname);

  Optional<Member> findByUsername(String username);
  Optional<Member> findByEmail(String email);

}
