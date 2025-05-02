package me.suhsaechan.suhgrassplanter.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.suhsaechan.suhgrassplanter.model.dto.CustomUserDetails;
import me.suhsaechan.suhgrassplanter.repository.MemberRepository;
import me.suhsaechan.suhgrassplanter.util.exception.CustomException;
import me.suhsaechan.suhgrassplanter.util.exception.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // 로그인 식별자 EMAIL 사용
    return new CustomUserDetails(
        memberRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)));
  }
}
