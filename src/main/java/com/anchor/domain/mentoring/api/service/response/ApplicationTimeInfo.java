package com.anchor.domain.mentoring.api.service.response;

import com.anchor.domain.mentor.domain.ActiveStatus;
import com.anchor.domain.mentor.domain.MentorSchedule;
import com.anchor.global.util.type.DateTimeRange;
import com.anchor.global.util.type.TimeRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Getter
@NoArgsConstructor
public class ApplicationTimeInfo {


  private Map<LocalDate, List<TimeRange>> unavailableTimes;
  private Map<DayOfWeek, List<MentorActiveTime>> activeTimes;


  private ApplicationTimeInfo(Map<LocalDate, List<TimeRange>> unavailableTimes,
      Map<DayOfWeek, List<MentorActiveTime>> activeTimes) {
    this.unavailableTimes = unavailableTimes;
    this.activeTimes = activeTimes;
  }

  public static ApplicationTimeInfo create(List<DateTimeRange> unavailableTimes, List<MentorSchedule> schedules) {
    Map<LocalDate, List<TimeRange>> unavailableTimeMap = createUnavailableTimeMap(unavailableTimes);
    Map<DayOfWeek, List<MentorActiveTime>> activeTimeMap = createActiveTimeMap(schedules);
    return new ApplicationTimeInfo(unavailableTimeMap, activeTimeMap);
  }

  private static Map<LocalDate, List<TimeRange>> createUnavailableTimeMap(List<DateTimeRange> unavailableTimes) {
    Map<LocalDate, List<TimeRange>> unavailableTimeMap = new HashMap<>();
    unavailableTimes.forEach(dateTimeRange -> addUnavailableTimes(dateTimeRange, unavailableTimeMap));
    return unavailableTimeMap;
  }

  private static void addUnavailableTimes(DateTimeRange dateTimeRange,
      Map<LocalDate, List<TimeRange>> unavailableTimeMap) {
    LocalDate startDay = dateTimeRange.getFrom()
        .toLocalDate();
    TimeRange timeRange = TimeRange.of(dateTimeRange);
    List<TimeRange> unavailableTimes = unavailableTimeMap.computeIfAbsent(startDay, key -> new ArrayList<>());
    unavailableTimes.add(timeRange);
  }

  private static Map<DayOfWeek, List<MentorActiveTime>> createActiveTimeMap(List<MentorSchedule> schedules) {
    if (CollectionUtils.isEmpty(schedules)) {
      return Collections.emptyMap();
    }
    Map<DayOfWeek, List<MentorActiveTime>> activeTimeMap = new EnumMap<>(DayOfWeek.class);
    schedules.stream()
        .map(MentorActiveTime::of)
        .forEach(mentorActiveTime -> addMentorActiveTimes(mentorActiveTime, activeTimeMap));
    activeTimeMap.forEach((key, value) -> value
        .sort(Comparator.comparing(MentorActiveTime::getOpenTime)));
    return activeTimeMap;
  }

  private static void addMentorActiveTimes(MentorActiveTime mentorActiveTime,
      Map<DayOfWeek, List<MentorActiveTime>> activeTimeMap) {
    List<MentorActiveTime> mentorActiveTimes = activeTimeMap.computeIfAbsent(mentorActiveTime.dayOfWeek,
        key -> new ArrayList<>());
    mentorActiveTimes.add(mentorActiveTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplicationTimeInfo that)) {
      return false;
    }
    return Objects.equals(getUnavailableTimes(), that.getUnavailableTimes()) && Objects.equals(
        getActiveTimes(), that.getActiveTimes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUnavailableTimes(), getActiveTimes());
  }

  @Getter
  @NoArgsConstructor
  static class MentorActiveTime {

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
    @JsonProperty("open_time")
    private LocalTime openTime;

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
    @JsonProperty("close_time")
    private LocalTime closeTime;

    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;

    @JsonProperty("active_status")
    private ActiveStatus activeStatus;


    private MentorActiveTime(LocalTime openTime, LocalTime closeTime, DayOfWeek dayOfWeek, ActiveStatus activeStatus) {
      this.openTime = openTime;
      this.closeTime = closeTime;
      this.dayOfWeek = dayOfWeek;
      this.activeStatus = activeStatus;
    }

    static MentorActiveTime of(MentorSchedule mentorSchedule) {
      return new MentorActiveTime(mentorSchedule.getOpenTime(), mentorSchedule.getCloseTime(),
          mentorSchedule.getDayOfWeek(), mentorSchedule.getActiveStatus());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof MentorActiveTime that)) {
        return false;
      }
      return Objects.equals(getOpenTime(), that.getOpenTime()) && Objects.equals(getCloseTime(),
          that.getCloseTime()) && getDayOfWeek() == that.getDayOfWeek() && getActiveStatus() == that.getActiveStatus();
    }

    @Override
    public int hashCode() {
      return Objects.hash(getOpenTime(), getCloseTime(), getDayOfWeek(), getActiveStatus());
    }
  }

}
