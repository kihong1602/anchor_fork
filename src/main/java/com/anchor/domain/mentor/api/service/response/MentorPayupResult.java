package com.anchor.domain.mentor.api.service.response;

import com.anchor.domain.payment.domain.PayupStatus;
import com.anchor.global.util.type.DateTimeRange;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MentorPayupResult {

  private Map<LocalDateTime, PayupTotalAmount> dailyTotalAmount;
  private Map<LocalDateTime, List<PayupInfo>> dailyMentoringPayupInfo;

  private MentorPayupResult(Map<LocalDateTime, PayupTotalAmount> dailyTotalAmount,
      Map<LocalDateTime, List<PayupInfo>> dailyMentoringPayupInfo) {
    this.dailyTotalAmount = dailyTotalAmount;
    this.dailyMentoringPayupInfo = dailyMentoringPayupInfo;
  }

  public static MentorPayupResult of(List<PayupInfo> payupInfos) {
    Map<LocalDateTime, PayupTotalAmount> dailyTotalAmount = new HashMap<>();
    Map<LocalDateTime, List<PayupInfo>> dailyMentoringPayupInfo = new HashMap<>();
    payupInfos.forEach(info -> {
      LocalDateTime startDateTime = info.getDateTimeRange()
          .getFrom()
          .truncatedTo(ChronoUnit.DAYS);
      dailyMentoringPayupInfo.computeIfAbsent(startDateTime, key -> new ArrayList<>())
          .add(info);
      dailyTotalAmount.computeIfAbsent(startDateTime, key -> new PayupTotalAmount())
          .sum(info.getPayupStatus(), info.getPayupAmount());
    });
    return new MentorPayupResult(dailyTotalAmount, dailyMentoringPayupInfo);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MentorPayupResult that)) {
      return false;
    }
    return Objects.equals(getDailyTotalAmount(), that.getDailyTotalAmount()) && Objects.equals(
        getDailyMentoringPayupInfo(), that.getDailyMentoringPayupInfo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDailyTotalAmount(), getDailyMentoringPayupInfo());
  }

  @Getter
  @NoArgsConstructor
  public static class PayupInfo {

    private DateTimeRange dateTimeRange;
    private String menteeNickname;
    private Integer paymentAmount;
    private Integer payupAmount;
    private PayupStatus payupStatus;

    public PayupInfo(LocalDateTime startDateTime, LocalDateTime endDateTime, String nickname, Integer paymentAmount,
        Integer payupAmount,
        PayupStatus payupStatus) {
      this.dateTimeRange = DateTimeRange.of(startDateTime, endDateTime);
      this.menteeNickname = nickname;
      this.paymentAmount = paymentAmount;
      this.payupAmount = payupAmount;
      this.payupStatus = payupStatus;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PayupInfo payupInfo)) {
        return false;
      }
      return Objects.equals(getDateTimeRange(), payupInfo.getDateTimeRange()) && Objects.equals(
          getMenteeNickname(), payupInfo.getMenteeNickname()) && Objects.equals(getPaymentAmount(),
          payupInfo.getPaymentAmount()) && Objects.equals(getPayupAmount(), payupInfo.getPayupAmount())
          && getPayupStatus() == payupInfo.getPayupStatus();
    }

    @Override
    public int hashCode() {
      return Objects.hash(getDateTimeRange(), getMenteeNickname(), getPaymentAmount(), getPayupAmount(),
          getPayupStatus());
    }
  }

  @Getter
  static class PayupTotalAmount {

    private final Map<PayupStatus, Integer> totalAmount = new EnumMap<>(PayupStatus.class);

    public void sum(PayupStatus status, Integer amount) {
      totalAmount.merge(status, amount, Integer::sum);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PayupTotalAmount that)) {
        return false;
      }
      return Objects.equals(getTotalAmount(), that.getTotalAmount());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getTotalAmount());
    }
  }
}
