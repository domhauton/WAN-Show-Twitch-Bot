package bot.util;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import bot.util.config.AppInjector;
import bot.util.config.Environment;

/**
 * Created by Dominic Hauton on 16/03/2016.
 *
 * Test for Date Time Util libraries
 */
public class DateTimeUtilTest {

  private static DateTimeUtil dateTimeUtil;

  @BeforeClass
  public static void setUp() {
    Injector injector = Guice.createInjector(new AppInjector(Environment.PROD));
    dateTimeUtil = injector.getInstance(DateTimeUtil.class);
  }

  @Test
  public void periodToStringTestEmpty() {
    Period periodOfZero = new Period(0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfZero);
    Assert.assertEquals("shortly", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestLow() {
    Period periodOfLow = new Period(0, 0, 10, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("shortly", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestMin1() {
    Period periodOfLow = new Period(0, 10, 10, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("10 minutes", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestMin2() {
    Period periodOfLow = new Period(0, 10, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("10 minutes", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestHour1() {
    Period periodOfLow = new Period(10, 0, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("10 hours", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestHour2() {
    Period periodOfLow = new Period(10, 10, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("10 hours and 10 minutes", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestDay1() {
    Period periodOfLow = new Period(0, 0, 0, 1, 0, 0, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("1 day", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestDay2() {
    Period periodOfLow = new Period(0, 0, 0, 1, 3, 0, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("1 day and 3 hours", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestDay3() {
    Period periodOfLow = new Period(0, 0, 0, 1, 3, 1, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("1 day, 3 hours and 1 minute", actualPrintedPeriod);
  }

  @Test
  public void periodToStringTestDay4() {
    Period periodOfLow = new Period(0, 0, 1, 1, 3, 1, 0, 0).normalizedStandard();
    String actualPrintedPeriod = DateTimeUtil.convertPeriodToHumanReadableString(periodOfLow);
    Assert.assertEquals("8 days, 3 hours and 1 minute", actualPrintedPeriod);
  }
}
