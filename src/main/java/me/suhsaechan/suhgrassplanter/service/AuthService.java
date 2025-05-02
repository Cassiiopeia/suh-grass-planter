package me.suhsaechan.suhgrassplanter.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhgrassplanter.model.constants.JwtTokenType;
import me.suhsaechan.suhgrassplanter.model.constants.Role;
import me.suhsaechan.suhgrassplanter.model.dto.CustomUserDetails;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;
import me.suhsaechan.suhgrassplanter.model.request.AuthRequest;
import me.suhsaechan.suhgrassplanter.model.response.AuthResponse;
import me.suhsaechan.suhgrassplanter.repository.MemberRepository;
import me.suhsaechan.suhgrassplanter.util.JwtUtil;
import me.suhsaechan.suhgrassplanter.util.exception.CustomException;
import me.suhsaechan.suhgrassplanter.util.exception.ErrorCode;
import me.suhsaechan.suhlogger.util.SuhLogger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final MemberRepository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * 회원가입
   */
  public AuthResponse register(AuthRequest request) {

    // 중복 닉네임 검증
    if (memberRepository.existsByNickname(request.getNickname())) {
      log.error("이미 사용중인 닉네임입니다. 요청 아이디: {}", request.getNickname());
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    // 사용자 저장
    Member savedMember = memberRepository.save(
        Member.builder()
            .password(bCryptPasswordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .nickname(request.getNickname())
            .role(Role.ROLE_USER)
            .build());

    SuhLogger.superLog(savedMember);

    return AuthResponse.builder()
        .member(savedMember)
        .build();
  }

  /**
   * 이메일 중복 확인
   */
  public boolean duplicateEmail(String email) {
    try{
      return memberRepository.existsByEmail(email);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 닉네임 중복 확인
   */
  public boolean duplicateNickname(String nickname) {
    try{
      return memberRepository.existsByNickname(nickname);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 로그인
   */
  public AuthResponse login(AuthRequest request) {
    // email로 사용자 조회하여 존재 여부 확인
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // Authentication 생성
    // 여기서 AuthenticationManager가 CustomUserDetailsService.loadUserByUsername 호출
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    // Member 추출
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    // 앞에서 이미 member를 조회했으므로 이 코드는 필요 없을 수 있지만,
    // 인증이 완료된 사용자 정보를 사용하는 것이 더 안전함
    member = userDetails.getMember();

    // 토큰 생성
    // AccessToken 발급
    String accessToken = jwtUtil.generateToken(authentication, JwtTokenType.ACCESS);
    // RefreshToken 발급
    String refreshToken = jwtUtil.generateToken(authentication, JwtTokenType.REFRESH);

    //토큰 반환
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .nickname(member.getNickname())
        .build();
  }

}