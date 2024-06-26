package com.anchor.domain.mentor.api.controller;

import com.anchor.domain.mentor.api.service.MentorService;
import com.anchor.domain.mentor.api.service.response.AppliedMentoringSearchResult;
import com.anchor.domain.mentor.api.service.response.MentorOpenCloseTimes;
import com.anchor.domain.mentor.domain.Career;
import com.anchor.global.auth.SessionUser;
import com.anchor.global.util.BankCode;
import com.anchor.global.util.view.ViewResolver;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/mentors")
@RequiredArgsConstructor
@Controller
public class MentorViewController {

  private final MentorService mentorService;
  private final ViewResolver viewResolver;

  @GetMapping("/register")
  public String register(Model model) {
    model.addAttribute("bankCodes", BankCode.values());
    model.addAttribute("careers", Career.values());
    return viewResolver.getViewPath("mentor", "register");
  }

  @GetMapping("/me/schedule")
  public String getUnavailableTimes(HttpSession session, Model model) {
    SessionUser sessionUser = SessionUser.getSessionUser(session);
    MentorOpenCloseTimes result = mentorService.getMentorSchedule(sessionUser.getMentorId());
    model.addAttribute("openCloseTimes", result);
    return viewResolver.getViewPath("mentor", "schedule");
  }

  @GetMapping("/me/applied-mentorings")
  public String getMentoringApplications(
      @PageableDefault(sort = {"id"}, direction = Direction.DESC) Pageable pageable, HttpSession session, Model model) {
    SessionUser user = SessionUser.getSessionUser(session);
    Page<AppliedMentoringSearchResult> results = mentorService.getMentoringApplications(1L, pageable);
    model.addAttribute("mentoringApplications", results);
    return viewResolver.getViewPath("mentor", "mentoring-application");
  }

  @GetMapping("/me/payup")
  public String viewPayupPage() {
    return viewResolver.getViewPath("mentor", "payup");
  }

}
