package me.suhsaechan.suhgrassplanter.service;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;
import static org.junit.jupiter.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhgrassplanter.model.postgres.GitHubProfile;
import me.suhsaechan.suhgrassplanter.util.EncryptionUtil;
import me.suhsaechan.suhlogger.util.SuhLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@Slf4j
class GitHubServiceTest {

  @Autowired
  GitHubService gitHubService;

  @Test
  public void mainTest() {
    lineLog("테스트 시작");

    timeLog(this::autoCommit_테스트);

    lineLog("테스트 종료");
  }

  public void autoCommit_테스트() throws Exception {
    // 테스트용 GitHubProfile 생성
    GitHubProfile profile = new GitHubProfile();
    profile.setGithubUsername("Cassiiopeia");
    profile.setEncryptedPat(EncryptionUtil.encrypt("YOUR_PERSONAL_ACCESS_TOKEN")); // PAT 키 암호화

    // 테스트용 레포지토리 이름
    String repository = "auto-commit";

    // autoCommit 메서드 호출
    gitHubService.autoCommit(profile, repository);

    lineLog("Auto-commit 테스트 완료!");
  }


}