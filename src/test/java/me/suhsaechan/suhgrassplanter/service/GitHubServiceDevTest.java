package me.suhsaechan.suhgrassplanter.service;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.superLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhgrassplanter.model.constants.Role;
import me.suhsaechan.suhgrassplanter.model.postgres.GithubProfile;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;
import me.suhsaechan.suhgrassplanter.repository.GithubProfileRepository;
import me.suhsaechan.suhgrassplanter.repository.MemberRepository;
import me.suhsaechan.suhgrassplanter.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@Slf4j
class GitHubServiceDevTest {

  @Autowired
  GitHubService gitHubService;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  GithubProfileRepository githubProfileRepository;

  @Value("${test.github.suhsaechan.username}")
  private String githubUsername;

  @Value("${test.github.suhsaechan.pat}")
  private String githubPat;

  @Value("${test.github.suhsaechan.email}")
  private String userEmail;

  @Value("${test.github.suhsaechan.repository}")
  private String githubRepository;

  @Test
  public void mainTest() {
    lineLog("테스트 시작");

//    timeLog(this::autoCommit_테스트);
    timeLog(this::checkTodayCommit_테스트);

    lineLog("테스트 종료");
  }

  private void checkTodayCommit_테스트() throws Exception {
    // Member 생성
    Member member = Member.builder()
        .email(userEmail)
        .nickname("testNickname")
        .password("testPassword")
        .role(Role.ROLE_USER)
        .build();

    Member savedMember = memberRepository.save(member);

    // GithubProfile 생성 및 Member 연결
    GithubProfile profile = GithubProfile.builder()
        .githubUsername(githubUsername)
        .encryptedPat(EncryptionUtil.encrypt(githubPat))
        .member(savedMember)
        .build();

    // GithubProfile 저장
    GithubProfile savedProfile = githubProfileRepository.save(profile);

    int contributionLevel = gitHubService.checkContributionLevel(
        profile.getGithubUsername(),
        LocalDate.now());

    superLog(contributionLevel);
  }

  public void autoCommit_테스트() throws Exception {
    // Member 먼저 생성
    Member member = Member.builder()
        .email(userEmail)
        .nickname("testNickname")
        .password("testPassword")
        .role(Role.ROLE_USER)
        .build();

    Member savedMember = memberRepository.save(member);

    // GithubProfile 생성 및 Member 연결
    GithubProfile profile = GithubProfile.builder()
        .githubUsername(githubUsername)
        .encryptedPat(EncryptionUtil.encrypt(githubPat))
        .member(savedMember)
        .build();

    // GithubProfile 저장
    GithubProfile savedProfile = githubProfileRepository.save(profile);

    // 테스트 레포지토리 이름
    String repository = githubRepository;

    // autoCommit
    gitHubService.autoCommit(savedProfile, repository);
  }

}