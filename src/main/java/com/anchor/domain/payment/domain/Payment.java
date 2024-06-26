package com.anchor.domain.payment.domain;

import com.anchor.domain.mentoring.api.controller.request.MentoringApplicationInfo;
import com.anchor.domain.mentoring.domain.MentoringApplication;
import com.anchor.global.payment.portone.response.PaymentCancelResult;
import com.anchor.global.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment")
public class Payment extends BaseEntity {

  @Column(name = "imp_uid", nullable = false, unique = true)
  private String impUid;

  @Column(name = "merchant_uid", nullable = false, unique = true)
  private String merchantUid;

  @Column(name = "order_uid", nullable = false, unique = true)
  private String orderUid;

  @Column(nullable = false)
  private Integer amount;

  @Column(name = "cancel_amount", nullable = false)
  private Integer cancelAmount = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

  @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY, optional = false)
  private MentoringApplication mentoringApplication;

  @Builder
  private Payment(String impUid, String merchantUid, Integer amount, MentoringApplication mentoringApplication) {
    this.impUid = impUid;
    this.merchantUid = merchantUid;
    this.orderUid = "anchor_" + merchantUid.substring(merchantUid.indexOf('_'));
    this.amount = amount;
    this.mentoringApplication = mentoringApplication;
  }

  public Payment(MentoringApplicationInfo applicationInfo) {
    this.impUid = applicationInfo.getImpUid();
    this.merchantUid = applicationInfo.getMerchantUid();
    this.orderUid = "anchor" + applicationInfo.getMerchantUid()
        .substring(merchantUid.indexOf('_'));
    this.amount = applicationInfo.getAmount();
    this.cancelAmount = 0;
    this.paymentStatus = PaymentStatus.SUCCESS;
  }

  public boolean isCancelled() {
    return this.paymentStatus.equals(PaymentStatus.CANCELLED);
  }

  public void editPaymentCancelStatus(PaymentCancelResult paymentCancelResult) {
    this.paymentStatus = PaymentStatus.CANCELLED;
    this.cancelAmount = paymentCancelResult.getCancelAmount();
  }

  public void addMentoringApplication(MentoringApplication mentoringApplication) {
    this.mentoringApplication = mentoringApplication;
  }
}
