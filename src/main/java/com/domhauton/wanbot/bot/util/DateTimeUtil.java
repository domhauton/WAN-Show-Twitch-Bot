package com.domhauton.wanbot.bot.util;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Created by Dominic Hauton on 15/03/2016.
 *
 * Static Utility class for DateTimes.
 */
public final class DateTimeUtil {

  private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
      .printZeroNever()
      .appendDays()
      .appendSuffix(" day", " days")
      .appendSeparator(", ", " and ")
      .appendHours()
      .appendSuffix(" hour", " hours")
      .appendSeparator(" and ")
      .appendMinutes()
      .appendSuffix(" minute", " minutes")
      .toFormatter();

  public static String convertPeriodToHumanReadableString(Period period) {
    if (period.toStandardSeconds().getSeconds() < 60) {
      return "shortly";
    } else {
      return period
          .withDays(period.toStandardDays().getDays())
          .withWeeks(0)
          .withMonths(0)
          .withYears(0)
          .withPeriodType(PeriodType.dayTime())
          .toString(periodFormatter);
    }
  }
}
