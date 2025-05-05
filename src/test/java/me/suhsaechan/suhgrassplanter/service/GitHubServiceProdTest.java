package me.suhsaechan.suhgrassplanter.service;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.superLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhgrassplanter.model.postgres.GitHubProfile;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;
import me.suhsaechan.suhgrassplanter.repository.MemberRepository;
import me.suhsaechan.suhgrassplanter.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("prod")
@Slf4j
class GitHubServiceProdTest {

  @Autowired
  GitHubService gitHubService;

  @Autowired
  MemberRepository memberRepository;

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

    timeLog(this::memberCreate_테스트);
//    timeLog(this::autoCommit_테스트);
//    timeLog(this::checkTodayCommit_테스트);

    lineLog("테스트 종료");
  }

  private void memberCreate_테스트() throws Exception {

    /**
     * 새찬 Member 생성
     */

    // 1. GitHubProfile 생성
    GitHubProfile profile = GitHubProfile.builder()
        .githubUsername(githubUsername)
        .encryptedPat(EncryptionUtil.encrypt(githubPat))
        .build();

    // 2. Member 생성 및 GitHubProfile 연결
    Member member = Member.builder()
        .email(userEmail)
        .nickname("testNickname")
        .password("testPassword")
        .githubProfile(profile)
        .build();

    // 3. GitHubProfile에 Member 연결
    profile.setMember(member);

    // 4. 새찬 Member 저장
    Member savedMember = memberRepository.save(member);

    superLog(savedMember);
  }

  private void checkTodayCommit_테스트() throws Exception {
    // GitHubProfile 생성
    GitHubProfile profile = GitHubProfile.builder()
        .githubUsername(githubUsername)
        .encryptedPat(EncryptionUtil.encrypt(githubPat))
        .build();

    // Member 생성 및 GitHubProfile 연결
    Member member = Member.builder()
        .email(userEmail)
        .nickname("testNickname")
        .password("testPassword")
        .githubProfile(profile)
        .build();

    // GitHubProfile에 Member 연결
    profile.setMember(member);

    // Member 저장
    Member savedMember = memberRepository.save(member);

    int contributionLevel = gitHubService.checkContributionLevel(
        savedMember.getGithubProfile().getGithubUsername(),
        LocalDate.now());

    superLog(contributionLevel);
  }

  public void autoCommit_테스트() throws Exception {
    // GitHubProfile 생성
    GitHubProfile profile = GitHubProfile.builder()
        .githubUsername(githubUsername)
        .encryptedPat(EncryptionUtil.encrypt(githubPat))
        .build();

    // Member 생성 및 GitHubProfile 연결
    Member member = Member.builder()
        .email(userEmail)
        .nickname("testNickname")
        .password("testPassword")
        .githubProfile(profile)
        .build();

    // GitHubProfile에 Member 연결
    profile.setMember(member);

    // Member 저장
    memberRepository.save(member);

    // 테스트 레포지토리 이름
    String repository = githubRepository;

    // autoCommit
    gitHubService.autoCommit(profile, repository);
  }


}