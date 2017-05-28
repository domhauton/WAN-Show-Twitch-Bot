package bot.util;

import org.joda.time.Period;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Dominic Hauton on 16/03/2016.
 * <p>
 * Test for Date Time Util libraries
 */
class DateTimeUtilTest {

  private final static DateTimeUtil dateTimeUtil = new DateTimeUtil();

  @Test
  void periodToStringTestEmpty() {
    Period periodOfZero = new Period(0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfZero);
    Assertions.assertEquals("shortly", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestLow() {
    Period periodOfLow = new Period(0, 0, 10, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("shortly", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestMin1() {
    Period periodOfLow = new Period(0, 10, 10, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("10 minutes", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestMin2() {
    Period periodOfLow = new Period(0, 10, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("10 minutes", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestHour1() {
    Period periodOfLow = new Period(10, 0, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("10 hours", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestHour2() {
    Period periodOfLow = new Period(10, 10, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("10 hours and 10 minutes", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestDay1() {
    Period periodOfLow = new Period(0, 0, 0, 1, 0, 0, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("1 day", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestDay2() {
    Period periodOfLow = new Period(0, 0, 0, 1, 3, 0, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("1 day and 3 hours", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestDay3() {
    Period periodOfLow = new Period(0, 0, 0, 1, 3, 1, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("1 day, 3 hours and 1 minute", actualPrintedPeriod);
  }

  @Test
  void periodToStringTestDay4() {
    Period periodOfLow = new Period(0, 0, 1, 1, 3, 1, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assertions.assertEquals("8 days, 3 hours and 1 minute", actualPrintedPeriod);
  }
}
