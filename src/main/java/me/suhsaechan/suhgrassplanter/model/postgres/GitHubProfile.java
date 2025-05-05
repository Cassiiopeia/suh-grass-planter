package me.suhsaechan.suhgrassplanter.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GitHubProfile extends BasePostgresEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID githubProfileId;

  @Column(nullable = false, unique = true)
  private String githubUsername;

  @Column(nullable = false)
  private String encryptedPat;

  @OneToOne(mappedBy = "githubProfile")
  private Member member;
}
