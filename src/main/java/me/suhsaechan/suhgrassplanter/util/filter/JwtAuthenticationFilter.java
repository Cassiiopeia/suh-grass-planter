package me.suhsaechan.suhgrassplanter.util.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import me.suhsaechan.suhgrassplanter.config.SecurityUrls;
import me.suhsaechan.suhgrassplanter.service.CustomUserDetailsService;
import me.suhsaechan.suhgrassplanter.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public JwtAuthenticationFilter(JwtUtil jwtUtil,
      CustomUserDetailsService customUserDetailsService) {
    this.jwtUtil = jwtUtil;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // AUTH_WHITELIST URL -? JWT 인증 안함
    String requestUri = request.getRequestURI();
    boolean isWhitelisted = SecurityUrls.AUTH_WHITELIST.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    if (isWhitelisted) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = getTokenStrFromBearer(request);
    if (token != null && jwtUtil.validateToken(token)) {
      // 토큰에서 username 추출
      String username = jwtUtil.getUsernameFromToken(token);
      // username -> 사용자 정보를 조회
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      if (userDetails != null) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  private String getTokenStrFromBearer(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
