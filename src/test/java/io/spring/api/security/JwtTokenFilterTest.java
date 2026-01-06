package io.spring.api.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class JwtTokenFilterTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtService jwtService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtTokenFilter jwtTokenFilter;

  private User user;
  private String validToken;
  private String userId;

  @BeforeEach
  public void setUp() {
    SecurityContextHolder.clearContext();
    userId = "test-user-id";
    validToken = "valid-jwt-token";
    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  public void should_authenticate_user_with_valid_token() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token " + validToken);
    when(jwtService.getSubFromToken(eq(validToken))).thenReturn(Optional.of(userId));
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    Assertions.assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  @Test
  public void should_continue_filter_chain_without_authentication_when_no_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).getSubFromToken(any());
  }

  @Test
  public void should_continue_filter_chain_without_authentication_when_header_is_empty() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("");

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).getSubFromToken(any());
  }

  @Test
  public void should_continue_filter_chain_without_authentication_when_header_has_no_token() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token");

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).getSubFromToken(any());
  }

  @Test
  public void should_continue_filter_chain_without_authentication_when_token_is_invalid() throws Exception {
    String invalidToken = "invalid-token";
    when(request.getHeader("Authorization")).thenReturn("Token " + invalidToken);
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(userRepository, never()).findById(any());
  }

  @Test
  public void should_continue_filter_chain_without_authentication_when_user_not_found() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token " + validToken);
    when(jwtService.getSubFromToken(eq(validToken))).thenReturn(Optional.of(userId));
    when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void should_not_override_existing_authentication() throws Exception {
    User existingUser = new User("existing@example.com", "existinguser", "password", "bio", "image");
    org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            existingUser, null, java.util.Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getHeader("Authorization")).thenReturn("Token " + validToken);
    when(jwtService.getSubFromToken(eq(validToken))).thenReturn(Optional.of(userId));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertEquals(existingUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    verify(userRepository, never()).findById(any());
  }

  @Test
  public void should_handle_bearer_prefix_in_authorization_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtService.getSubFromToken(eq(validToken))).thenReturn(Optional.of(userId));
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    Assertions.assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  @Test
  public void should_handle_multiple_spaces_in_authorization_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token " + validToken + " extra");
    when(jwtService.getSubFromToken(eq(validToken))).thenReturn(Optional.of(userId));
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
