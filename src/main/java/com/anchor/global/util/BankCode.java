package com.anchor.global.util;

import com.anchor.global.exception.type.api.BankNameNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankCode {

  HANA("하나은행", "081"),
  KDB("산업은행", "002"),
  IBK("기업은행", "003"),
  KB("국민은행", "004"),
  NH("농협은행", "011"),
  WOORI("우리은행", "020"),
  SC_KOREA("제일은행", "023"),
  CITY("시티은행", "027"),
  DAEGU("대구은행", "032"),
  KWANGJU("광주은행", "034"),
  JEJU("제주은행", "035"),
  JEONBUK("전북은행", "037"),
  BNK_KYONGNAM("경남은행", "039"),
  MG("새마을금고", "045"),
  SHINHAN("신한은행", "088"),
  KAKAO("카카오뱅크", "090");


  private static final Map<String, BankCode> BANK_CODES = Collections.unmodifiableMap(Arrays.stream(values())
      .collect(Collectors.toMap(BankCode::getBankName, Function.identity())));

  private final String bankName;

  private final String code;

  public static BankCode find(String bankName) {
    return Optional.ofNullable(BANK_CODES.get(bankName))
        .orElseThrow(BankNameNotFoundException::new);
  }

}