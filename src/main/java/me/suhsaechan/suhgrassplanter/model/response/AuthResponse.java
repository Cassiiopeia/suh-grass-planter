package me.suhsaechan.suhgrassplanter.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.suhsaechan.suhgrassplanter.model.postgres.Member;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuthResponse {

  private String refreshToken;
  private String accessToken;
  private String nickname;
  private Member member;

}
