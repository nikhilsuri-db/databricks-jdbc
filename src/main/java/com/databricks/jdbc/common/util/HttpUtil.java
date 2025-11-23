package com.databricks.jdbc.common.util;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public class HttpUtil {
  /** Check if the HTTP response is successful */
  public static boolean isSuccessfulHttpResponse(CloseableHttpResponse response) {
    return response.getCode() >= 200 && response.getCode() < 300;
  }
}
