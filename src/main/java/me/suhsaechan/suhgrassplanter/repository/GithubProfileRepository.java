package me.suhsaechan.suhgrassplanter.repository;

import me.suhsaechan.suhgrassplanter.model.postgres.GithubProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GithubProfileRepository extends JpaRepository<GithubProfile, UUID> {
  List<GithubProfile> findByMember_MemberId(UUID memberId);

  // 모든 GitHub 프로필을 회원 정보와 함께 가져오는 메서드
  @Query("SELECT g FROM GithubProfile g JOIN FETCH g.member")
  List<GithubProfile> findAllWithMember();
}