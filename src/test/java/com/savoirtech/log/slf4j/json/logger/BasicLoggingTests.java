package com.savoirtech.log.slf4j.json.logger;

import com.savoirtech.log.slf4j.json.LoggerFactory;

import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BasicLoggingTests {

  String dateFormatString = "yyyy-MM-dd HH:mm:ss.SSSZ";
  FastDateFormat formatter = FastDateFormat.getInstance(dateFormatString);

  @Test
  public void itWorks() {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.trace().message("It works!").log();
    logger.debug().message("It works!").log();
    logger.info().message("It works!").log();
    logger.warn().message("It works!").log();
    logger.error().message("It works!").log();
  }

  @Test
  public void categoryEnabled() {
    String expectedLevelElement = "\"level\":\"INFO\"";
    String expectedMessageElement = "\"category\":\"My category\"";

    org.slf4j.Logger slf4jLogger = mock(org.slf4j.Logger.class);
    when(slf4jLogger.isInfoEnabled()).thenReturn(true);

    Logger logger = new Logger(slf4jLogger, formatter);

    logger.info().category("My category").log();

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

    verify(slf4jLogger).info(messageCaptor.capture());

    String actualMessage = messageCaptor.getValue();

    assert(actualMessage.contains(expectedLevelElement));
    assert(actualMessage.contains(expectedMessageElement));
    assert(actualMessage.startsWith("{"));
    assert(actualMessage.endsWith("}"));
  }

  @Test
  public void categoryDisabled() {
    org.slf4j.Logger slf4jLogger = mock(org.slf4j.Logger.class);
    when(slf4jLogger.isDebugEnabled()).thenReturn(false);

    Logger logger = new Logger(slf4jLogger, formatter);

    logger.info().category("My category").log();

    verify(slf4jLogger, times(0)).debug(anyString());
  }

  @Test
  public void allCollections() {
    String expectedLevelElement = "\"level\":\"TRACE\"";
    String expectedCategoryElement = "\"category\":\"My category\"";
    String expectedMessageElement = "\"message\":\"Report executed\"";
    String expectedMapElement = "\"someStats\":{\"numberSold\":\"0\"}";
    String expectedListElement = "\"customers\":[\"Acme\",\"Sun\"]";
    String expectedFieldElement = "\"year\":\"2016\"";

    org.slf4j.Logger slf4jLogger = mock(org.slf4j.Logger.class);
    when(slf4jLogger.isTraceEnabled()).thenReturn(true);

    Logger logger = new Logger(slf4jLogger, formatter);

    Map<String, String> map = new HashMap<>();
    map.put("numberSold", "0");

    List<String> list = new ArrayList<>();
    list.add("Acme");
    list.add("Sun");

    logger.trace()
        .category("My category")
        .message("Report executed")
        .map("someStats", map)
        .list("customers", list)
        .field("year", "2016")
        .log();

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

    verify(slf4jLogger).trace(messageCaptor.capture());

    String actualMessage = messageCaptor.getValue();

    assert(actualMessage.contains(expectedLevelElement));
    assert(actualMessage.contains(expectedCategoryElement));
    assert(actualMessage.contains(expectedMessageElement));
    assert(actualMessage.contains(expectedMapElement));
    assert(actualMessage.contains(expectedListElement));
    assert(actualMessage.contains(expectedFieldElement));
    assert(actualMessage.startsWith("{"));
    assert(actualMessage.endsWith("}"));
  }

  @Test
  public void fieldOverwritesCategory() {
    String unexpectedMessageElement = "\"category\":\"This gets overwritten\"";
    String expectedMessageElement = "\"category\":\"This wins\"";

    org.slf4j.Logger slf4jLogger = mock(org.slf4j.Logger.class);
    when(slf4jLogger.isWarnEnabled()).thenReturn(true);

    Logger logger = new Logger(slf4jLogger, formatter);

    logger.warn()
        .category("This gets overwritten")
        .field("category", "This wins")
        .log();

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

    verify(slf4jLogger).warn(messageCaptor.capture());

    String actualMessage = messageCaptor.getValue();

    assert(!actualMessage.contains(unexpectedMessageElement));
    assert(actualMessage.contains(expectedMessageElement));
  }

  @Test
  public void noExceptionThrown() {

  }

  @Test
  public void lambdas() {
    String expectedCategoryElement = "\"category\":\"Something expensive\"";

    org.slf4j.Logger slf4jLogger = mock(org.slf4j.Logger.class);
    when(slf4jLogger.isErrorEnabled()).thenReturn(true);

    Logger logger = new Logger(slf4jLogger, formatter);

    logger.error()
        .category(() -> "Something expensive")
        .log();

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

    verify(slf4jLogger).error(messageCaptor.capture());

    String actualMessage = messageCaptor.getValue();

    assert(actualMessage.contains(expectedCategoryElement));
  }
}