package me.suhsaechan.suhgrassplanter.model.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HashType {
  GITHUB_ISSUES("관리되는 Gihub Issue 에 대한 전체 해시값"),
  ;

  private final String description;
}
