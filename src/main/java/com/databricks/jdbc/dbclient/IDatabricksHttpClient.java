package com.databricks.jdbc.dbclient;

import com.databricks.jdbc.exception.DatabricksHttpException;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;

/** Http client interface for executing http requests with HTTP/2 support. */
public interface IDatabricksHttpClient {

  /**
   * Executes the given http request and returns the response
   *
   * @param request underlying http request
   * @return http response
   */
  CloseableHttpResponse execute(ClassicHttpRequest request) throws DatabricksHttpException;

  /**
   * Executes the given http request and returns the response
   *
   * @param request underlying http request
   * @param supportGzipEncoding whether to support gzip encoding header
   * @return http response
   */
  CloseableHttpResponse execute(ClassicHttpRequest request, boolean supportGzipEncoding)
      throws DatabricksHttpException;

  /**
   * Executes the given http request asynchronously and returns the future
   *
   * @param requestProducer request producer
   * @param responseConsumer response consumer
   * @param callback future callback
   * @return future
   * @param <T> type of the response
   */
  <T> Future<T> executeAsync(
      AsyncRequestProducer requestProducer,
      AsyncResponseConsumer<T> responseConsumer,
      FutureCallback<T> callback);
}
