package me.suhsaechan.suhgrassplanter.service;

import java.util.Base64;
import java.util.Optional;
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
import java.util.List;

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
    String url = "https://github.com/" + githubUsername;
    Request request = new Request.Builder().url(url).build();
    try (Response response = okHttpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      String html = response.body().string();
      Document doc = Jsoup.parse(html);
      Element cell = doc.selectFirst("td.ContributionCalendar-day[data-date='" + date.toString() + "']");
      if (cell != null) {
        String level = cell.attr("data-level");
        return Integer.parseInt(level);
      }
      return 0;
    }
  }

  public void autoCommit(GitHubProfile profile, String repository) throws Exception {
    String pat = EncryptionUtil.decrypt(profile.getEncryptedPat());
    String commitMessage = "Auto-commit: Updated changelog for daily maintenance";
    String changelogContent = "## [" + LocalDate.now().toString() + "]\n- " + commitMessage + "\n";

    Member member = memberRepository.findByGithubProfile(profile)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // GitHub API to update CHANGELOG.md
    OkHttpClient client = okHttpClient.newBuilder()
        .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + pat)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .build()))
        .build();

    // Get current CHANGELOG.md (if exists)
    String fileUrl =
        "https://api.github.com/repos/" + profile.getGithubUsername() + "/" + repository + "/contents/CHANGELOG.md";
    Request getFileRequest = new Request.Builder().url(fileUrl).build();
    String sha = null;
    String existingContent = "";
    try (Response getResponse = client.newCall(getFileRequest).execute()) {
      if (getResponse.isSuccessful()) {
        String json = getResponse.body().string();
        // Parse JSON to get SHA and content (simplified; use a JSON library like Jackson in production)
        sha = json.contains("\"sha\"") ? json.split("\"sha\":\"")[1].split("\"")[0] : null;
        existingContent = json.contains("\"content\"") ? new String(
            Base64.getDecoder().decode(json.split("\"content\":\"")[1].split("\"")[0])) : "";
      }
    }

    // Update content
    String newContent = existingContent + "\n" + changelogContent;
    String encodedContent = Base64.getEncoder().encodeToString(newContent.getBytes());

    // Prepare commit payload
    String payload = String.format(
        "{\"message\":\"%s\",\"content\":\"%s\",\"sha\":\"%s\"}",
        commitMessage, encodedContent, sha != null ? sha : ""
    );

    // Commit to GitHub
    RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));
    Request commitRequest = new Request.Builder()
        .url(fileUrl)
        .put(body)
        .build();

    try (Response commitResponse = client.newCall(commitRequest).execute()) {
      if (!commitResponse.isSuccessful()) {
        throw new IOException("Failed to commit: " + commitResponse);
      }

      // Log the commit
      CommitLog log = new CommitLog();
      log.setMember(member);
      log.setRepository(repository);
      log.setCommitMessage(commitMessage);
      commitLogRepository.save(log);
    }
  }

  // 매일 PM 11:50 실행
//  @Scheduled(cron = "0 50 23 * * ?")
  public void checkContributions() {
    List<Member> members = memberRepository.findAll();
    for (Member member : members) {
      GitHubProfile profile = member.getGithubProfile();
      if (profile != null) {
        try {
          int level = checkContributionLevel(profile.getGithubUsername(), LocalDate.now());
          if (level == 0) {
            autoCommit(profile, "auto-commit"); //TODO: 실제 레포이름으로 변경 필요
          }
        } catch (Exception e) {
          // Log error
          System.err.println("Error processing " + profile.getGithubUsername() + ": " + e.getMessage());
        }
      }
    }
  }
}
