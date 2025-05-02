package me.suhsaechan.suhgrassplanter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.suhsaechan.suhgrassplanter.model.constants.Author;
import me.suhsaechan.suhgrassplanter.model.request.AuthRequest;
import me.suhsaechan.suhgrassplanter.model.response.AuthResponse;
import me.suhsaechan.suhgrassplanter.service.AuthService;
import me.suhsaechan.suhgrassplanter.util.docs.ApiChangeLog;
import me.suhsaechan.suhgrassplanter.util.docs.ApiChangeLogs;
import me.suhsaechan.suhlogger.annotation.LogMonitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@Tag(
    name = "회원가입, 로그인 API",
    description = "회원가입, 로그인 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  /**
   * 회원가입
   * **/
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-08",
          author = Author.SUHSAECHAN,
          issueNumber = 1,
          description = "프로젝트 초기설정 회원가입 기능 추가"
      )
  })
  @Operation(
      summary = "회원가입",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (RegisterRequest)
          - **`username`**: 회원 ID
          - **`password`**: 회원 비밀번호 
          - **`nickname`**: 회원 닉네입 
          - **`email`**: 회원 이메일 
          - **`gender`**: 회원 성별 (gender 로 MALE, FEMALE 로 받아야함)
          - **`age`**: 나이 (18세 이상 90세 이하)
          
          ## 반환값 (ResponseEntity<RegisterResponse>)
          - **`memberId`**: 회원 고유 코드 
          - **`username`**: 회원 ID
          - **`nickname`**: 회원 닉네입 
          - **`email`**: 회원 이메일 
          
          ## 에러코드
          - **`DUPLICATE_NICKNAME`**: 이미 존재하는 닉네임입니다.
          - **`INVALID_AGE`**: 잘못된 나이입니다.
          - 
          """
  )

  @PostMapping("/register")
  @LogMonitor
  public ResponseEntity<AuthResponse> register(
      @RequestBody AuthRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * EMAIL 중복 확인
   * **/
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-17",
          author = Author.SUHSAECHAN,
          issueNumber = 1,
          description = "프로젝트 초기설정 이메일 중복확인 기능 추가"
      )})
  @Operation(summary = "이메일 중복확인",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (String)
          - **`email`**: 회원 이메일
          
          ## 반환값 (ResponseEntity<Boolean>)
        - **중복일 경우**: true
        - **사용 가능한 경우**: false
          """)
  @GetMapping("/duplicate/email")
  @LogMonitor
  public Boolean duplicateEmail(@RequestParam String email) {
    return authService.duplicateEmail(email);
  }


  /**
   * 로그인
   * **/
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-08",
          author = Author.SUHSAECHAN,
          issueNumber = 32,
          description = "요청객체, 반환객체 개선, TODO: Service 로직 추가"
      )
  })
  @Operation(
      summary = "로그인",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (LoginRequest)
          - **`username`**: 회원 ID
          - **`password`**: 회원 비밀번호

          ## 반환값 (LoginResponse)
          - **`accessToken`**: 발급된 AccessToken
          - **`refreshToken`**: 발급된 RefreshToken
          - **`nickname`**: 회원 닉네임
          
          ## 에러코드
          - **`DUPLICATE_USERNAME`**: 이미 사용중인 아이디입니다.
          - **`INVALID_CREDENTIALS`**: 유효하지 않은 자격 증명입니다.
          - **`MEMBER_NOT_FOUND`**: 회원 정보를 찾을 수 없습니다.
          """
  )
  @PostMapping("/login")
  @LogMonitor
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  //아이디찾기

  //비밀번호 변경

  // 로그아웃







}
