package me.suhsaechan.suhgrassplanter.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AuthRequest {
  private String nickname;
  private String password;
  private String email;
  private String githubPat;
  private String githubUsername;

}
