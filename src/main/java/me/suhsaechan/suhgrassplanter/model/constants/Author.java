package me.suhsaechan.suhgrassplanter.model.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Swagger DOCS에서 @ApiChangeLog 어노테이션에 사용
@Getter
@AllArgsConstructor
public enum Author {
  SUHSAECHAN("서새찬");

  private final String displayName;
}
