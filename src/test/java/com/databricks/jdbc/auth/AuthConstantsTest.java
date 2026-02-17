package com.databricks.jdbc.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AuthConstantsTest {

  @Test
  void should_HaveCorrectGrantTypeRefreshTokenKey() {
    assertEquals("refresh_token", AuthConstants.GRANT_TYPE_REFRESH_TOKEN_KEY);
  }

  @Test
  void should_HaveCorrectGrantTypeKey() {
    assertEquals("grant_type", AuthConstants.GRANT_TYPE_KEY);
  }

  @Test
  void should_HaveConsistentConstantValues() {
    // Verify constants haven't been accidentally changed
    assertNotNull(AuthConstants.GRANT_TYPE_REFRESH_TOKEN_KEY);
    assertFalse(AuthConstants.GRANT_TYPE_REFRESH_TOKEN_KEY.isEmpty());
    assertNotNull(AuthConstants.GRANT_TYPE_KEY);
    assertFalse(AuthConstants.GRANT_TYPE_KEY.isEmpty());
  }
}
