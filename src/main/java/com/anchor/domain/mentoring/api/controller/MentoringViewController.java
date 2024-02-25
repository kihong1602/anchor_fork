package com.anchor.domain.mentoring.api.controller;

import com.anchor.domain.mentoring.api.controller.request.MentoringApplicationTime;
import com.anchor.domain.mentoring.api.service.MentoringService;
import com.anchor.domain.mentoring.api.service.response.MentoringContents;
import com.anchor.domain.mentoring.api.service.response.MentoringDetailInfo;
import com.anchor.domain.mentoring.api.service.response.MentoringPayConfirmInfo;
import com.anchor.domain.mentoring.api.service.response.MentoringSearchInfo;
import com.anchor.domain.mentoring.api.service.response.MentoringSearchResult;
import com.anchor.domain.mentoring.api.service.response.PopularTag;
import com.anchor.global.auth.SessionUser;
import com.anchor.global.util.view.ViewResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@RequiredArgsConstructor
@RequestMapping("/mentorings")
@Controller
public class MentoringViewController {

  private final ViewResolver viewResolver;
  private final MentoringService mentoringService;

  /**
   * 검색어와 태그를 사용해 멘토링 목록을 조회합니다. 검색어 또는 태그 미입력시 전체 목록을 반환합니다.
   */
  @GetMapping
  public String viewMentoringPage(
      @RequestParam(value = "tag", required = false) List<String> tags,
      @RequestParam(value = "keyword", required = false) String keyword,
      @PageableDefault(size = 16, sort = "totalApplicationNumber", direction = Sort.Direction.DESC) Pageable pageable,
      Model model
  ) {
    Page<MentoringSearchResult> mentoringSearchResults = mentoringService.getMentorings(tags, keyword, pageable);
    List<PopularTag> popularMentoringTags = mentoringService.getPopularTags();
    MentoringSearchInfo mentoringSearchInfo = MentoringSearchInfo.of(mentoringSearchResults, popularMentoringTags);
    model.addAttribute("mentoringSearchInfo", mentoringSearchInfo);
    return viewResolver.getViewPath("mentoring", "mentoring-search");
  }

  /**
   * 멘토링 상세내용 페이지를 조회합니다.
   */
  @GetMapping("/{id}")
  public String viewMentoringDetailPage(@PathVariable("id") Long id, Model model) {
    List<PopularTag> popularTags = mentoringService.getPopularTags();
    MentoringDetailInfo mentoringDetailInfo = mentoringService.getMentoringDetailInfo(id);
    mentoringDetailInfo.addPopularTags(popularTags);
    model.addAttribute("mentoringDetail", mentoringDetailInfo);
    return viewResolver.getViewPath("mentoring", "mentoring-detail");
  }

  /**
   * 멘토링 생성 페이지를 조회합니다.
   */
  @PreAuthorize("hasRole('ROLE_MENTOR')")
  @GetMapping("/new")
  public String viewMentoringCreationPage() {
    return viewResolver.getViewPath("mentoring", "mentoring-new");
  }

  /**
   * 멘토링 편집 페이지를 조회합니다.
   */
  @PreAuthorize("hasRole('ROLE_MENTOR')")
  @GetMapping("/{id}/edit")
  public String viewMentoringEditPage(@PathVariable Long id, @SessionAttribute("user") SessionUser user, Model model) {
    MentoringContents result = mentoringService.getContents(id, user.getMentorId());
    model.addAttribute("mentoringContents", result);
    return viewResolver.getViewPath("mentoring", "mentoring-edit");
  }

  /**
   * 멘토링 결제페이지로 이동합니다. 결제할 멘토링정보, 신청한 시간이 데이터로 반환됩니다.
   */
  @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_MENTOR')")
  @PostMapping("/{id}/payment")
  public String viewMentoringPaymentPage(@PathVariable("id") Long id,
      @ModelAttribute MentoringApplicationTime applicationTime, @SessionAttribute("user") SessionUser user,
      Model model) {
    MentoringPayConfirmInfo mentoringConfirmInfo = mentoringService.getMentoringConfirmInfo(id, applicationTime, user);
    model.addAttribute("confirmInfo", mentoringConfirmInfo);
    return viewResolver.getViewPath("mentoring", "mentoring-payment");
  }

}
