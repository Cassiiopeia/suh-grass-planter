package me.suhsaechan.suhgrassplanter.service;

import static me.suhsaechan.suhlogger.util.SuhLogger.*;

import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhgrassplanter.model.postgres.CommitLog;
import me.suhsaechan.suhgrassplanter.model.postgres.GitHubProfile;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;
import me.suhsaechan.suhgrassplanter.repository.CommitLogRepository;
import me.suhsaechan.suhgrassplanter.repository.MemberRepository;
import me.suhsaechan.suhgrassplanter.util.EncryptionUtil;
import me.suhsaechan.suhgrassplanter.util.exception.CustomException;
import me.suhsaechan.suhgrassplanter.util.exception.ErrorCode;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class GitHubService {

  @Autowired
  private OkHttpClient okHttpClient;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CommitLogRepository commitLogRepository;

  public int checkContributionLevel(String githubUsername, LocalDate date) throws IOException {
    log.info("GitHub 기여도 확인 시작 - 사용자: {}, 날짜: {}", githubUsername, date);

    String url = "https://github.com/" + githubUsername;
    Request request = new Request.Builder().url(url).build();

    log.info("GitHub 프로필 요청 - URL: {}", url);

    try (Response response = okHttpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.error("GitHub API 응답 실패 - 상태 코드: {}", response.code());
        throw new IOException("Unexpected code " + response);
      }

      log.info("GitHub 응답 수신 성공 - 상태 코드: {}", response.code());
      String html = response.body().string();
      Document doc = Jsoup.parse(html);
      Element cell = doc.selectFirst("td.ContributionCalendar-day[data-date='" + date.toString() + "']");

      if (cell != null) {
        String level = cell.attr("data-level");
        int contributionLevel = Integer.parseInt(level);
        log.info("기여도 레벨 확인 완료 - 사용자: {}, 날짜: {}, 레벨: {}", githubUsername, date, contributionLevel);
        return contributionLevel;
      }

      log.info("해당 날짜의 기여도 정보 없음 - 사용자: {}, 날짜: {}", githubUsername, date);
      return 0;
    } catch (Exception e) {
      log.error("GitHub 기여도 확인 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  public void autoCommit(GitHubProfile profile, String repository) throws Exception {
    lineLog("자동 커밋 시작 - 사용자: " + profile.getGithubUsername() + ", 저장소: " + repository);

    log.info("PAT 복호화 진행 중...");
    String pat = EncryptionUtil.decrypt(profile.getEncryptedPat());
    log.info("PAT 복호화 완료");

    String commitMessage = "Auto-commit: Updated changelog for daily maintenance";
    String changelogContent = "## [" + LocalDate.now().toString() + "]\n- " + commitMessage + "\n";

    log.info("연관된 회원 정보 조회 중...");
    Member member = memberRepository.findByGithubProfile(profile)
        .orElseThrow(() -> {
          log.error("GitHub 프로필에 연결된 회원 정보 없음 - 프로필: {}", profile.getGithubUsername());
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
    log.info("연관된 회원 정보 조회 완료 - 회원: {}", member.getNickname());

    // GitHub API 클라이언트 설정
    log.info("GitHub API 클라이언트 설정 중...");
    OkHttpClient client = okHttpClient.newBuilder()
        .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + pat)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .build()))
        .build();
    log.info("GitHub API 클라이언트 설정 완료");

    // CHANGELOG.md 파일 조회
    String fileUrl =
        "https://api.github.com/repos/" + profile.getGithubUsername() + "/" + repository + "/contents/CHANGELOG.md";
    log.info("CHANGELOG.md 파일 조회 중 - URL: {}", fileUrl);

    Request getFileRequest = new Request.Builder().url(fileUrl).build();
    String sha = null;
    String existingContent = "";

    try (Response getResponse = client.newCall(getFileRequest).execute()) {
      if (getResponse.isSuccessful()) {
        log.info("CHANGELOG.md 파일 존재함 - 상태 코드: {}", getResponse.code());
        String json = getResponse.body().string();
        sha = json.contains("\"sha\"") ? json.split("\"sha\":\"")[1].split("\"")[0] : null;
        existingContent = json.contains("\"content\"") ? new String(
            Base64.getDecoder().decode(json.split("\"content\":\"")[1].split("\"")[0].replaceAll("\\\\n", ""))) : "";

        log.info("기존 파일 SHA: {}", sha);
        log.debug("기존 파일 내용 크기: {} 바이트", existingContent.length());
      } else {
        log.info("CHANGELOG.md 파일이 존재하지 않음 - 새로 생성 예정");
      }
    }

    // 파일 내용 업데이트
    String newContent = existingContent + "\n" + changelogContent;
    log.debug("업데이트된 내용 크기: {} 바이트", newContent.length());
    String encodedContent = Base64.getEncoder().encodeToString(newContent.getBytes());

    // 커밋 페이로드 준비
    String payload;
    if (sha != null) {
      payload = String.format(
          "{\"message\":\"%s\",\"content\":\"%s\",\"sha\":\"%s\"}",
          commitMessage, encodedContent, sha
      );
    } else {
      payload = String.format(
          "{\"message\":\"%s\",\"content\":\"%s\"}",
          commitMessage, encodedContent
      );
    }

    // GitHub에 커밋
    log.info("GitHub에 커밋 요청 중...");
    RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));
    Request commitRequest = new Request.Builder()
        .url(fileUrl)
        .put(body)
        .build();

    try (Response commitResponse = client.newCall(commitRequest).execute()) {
      if (!commitResponse.isSuccessful()) {
        log.error("커밋 실패 - 상태 코드: {}, 응답: {}",
            commitResponse.code(), commitResponse.body() != null ? commitResponse.body().string() : "응답 없음");
        throw new IOException("Failed to commit: " + commitResponse);
      }

      log.info("커밋 성공 - 상태 코드: {}", commitResponse.code());

      // 커밋 로그 저장
      log.info("커밋 로그 저장 중...");
      CommitLog commitLog = CommitLog.builder()
          .member(member)
          .Repository(repository)
          .commitMessage(commitMessage)
          .commitTime(LocalDateTime.now())
          .success(true)
          .build();

      CommitLog savedLog = commitLogRepository.save(commitLog);
      log.info("커밋 로그 저장 완료 - 로그 ID: {}", savedLog.getCommitLogId());

      superLog(savedLog);
    } catch (Exception e) {
      log.error("커밋 처리 중 예외 발생: {}", e.getMessage(), e);

      // 실패한 커밋 로그 저장
      CommitLog errorLog = CommitLog.builder()
          .member(member)
          .Repository(repository)
          .commitMessage(commitMessage)
          .commitTime(LocalDateTime.now())
          .success(false)
          .errorMessage(e.getMessage())
          .build();

      commitLogRepository.save(errorLog);
      throw e;
    }

    lineLog("자동 커밋 완료 - 사용자: " + profile.getGithubUsername() + ", 저장소: " + repository);
  }

  // 매일 PM 11:50 실행
  @Scheduled(cron = "0 50 23 * * ?")
  @Transactional
  public void checkContributions() {
    lineLog("일일 GitHub 기여도 확인 및 자동 커밋 시작");
    log.info("모든 회원의 GitHub 기여도 확인 작업 시작 - 현재 시간: {}", LocalDateTime.now());

    List<Member> members = memberRepository.findAll();
    log.info("총 회원 수: {}", members.size());

    int processedCount = 0;
    int commitCount = 0;

    for (Member member : members) {
      GitHubProfile profile = member.getGithubProfile();
      if (profile != null) {
        log.info("회원 처리 중 [{}/{}] - 회원: {}, GitHub: {}",
            ++processedCount, members.size(), member.getNickname(), profile.getGithubUsername());

        try {
          int level = checkContributionLevel(profile.getGithubUsername(), LocalDate.now());

          if (level == 0) {
            log.info("오늘 커밋이 없음 - 자동 커밋 실행 - 사용자: {}", profile.getGithubUsername());
            autoCommit(profile, "auto-commit"); // TODO: 실제 레포이름으로 변경 필요
            commitCount++;
          } else {
            log.info("오늘 이미 커밋 있음 (레벨: {}) - 자동 커밋 생략 - 사용자: {}", level, profile.getGithubUsername());
          }
        } catch (Exception e) {
          log.error("회원 처리 중 오류 발생 - 회원: {}, GitHub: {}, 오류: {}",
              member.getNickname(), profile.getGithubUsername(), e.getMessage(), e);
        }
      } else {
        log.warn("GitHub 프로필 정보 없음 - 회원: {}", member.getNickname());
      }
    }

    log.info("일일 GitHub 기여도 확인 완료 - 처리된 회원: {}, 자동 커밋: {}", processedCount, commitCount);
    lineLog("일일 GitHub 기여도 확인 및 자동 커밋 완료");
  }
}