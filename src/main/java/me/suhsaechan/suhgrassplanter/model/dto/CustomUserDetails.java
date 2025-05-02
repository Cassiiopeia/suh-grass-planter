package me.suhsaechan.suhgrassplanter.model.dto;


import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final Member member;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public String getUsername() {
    // 로그인 식별자 EMAIL 사용
    return member.getEmail();
  }

  public UUID getMemberId(){
    return this.member.getMemberId();
  }

  public Member getMember() {
    return member;
  }
}
