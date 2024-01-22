package com.anchor.domain.mentoring.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.beans.ConstructorProperties;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MentoringReviewInfo {

  @NotBlank(message = "상세 내용을 입력해주세요.")
  @Size(min = 1, max = 200, message = "1자 이상 200자 이하로 작성해주세요.")
  private String contents;

  private Integer ratings;

  @Builder
  @ConstructorProperties({"contents", "ratings"})
  public MentoringReviewInfo(String contents, Integer ratings) {
    this.contents = contents;
    this.ratings = ratings;
  }

}