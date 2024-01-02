package com.anchor.domain.user.api.service;

import com.anchor.domain.mentoring.domain.MentoringApplication;
import com.anchor.domain.mentoring.domain.MentoringStatus;
import com.anchor.domain.mentoring.domain.MentoringUnavailableTime;
import com.anchor.domain.mentoring.domain.repository.MentoringApplicationRepository;
import com.anchor.domain.mentoring.domain.repository.MentoringUnavailableTimeRepository;
import com.anchor.domain.user.api.controller.request.AppliedMentoringStatus;
import com.anchor.domain.user.api.service.response.AppliedMentoringInfo;
import com.anchor.domain.user.domain.User;
import com.anchor.domain.user.domain.repository.UserRepository;
import com.anchor.global.auth.SessionUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final MentoringApplicationRepository mentoringApplicationRepository;
  private final MentoringUnavailableTimeRepository mentoringUnavailableTimeRepository;

  @Transactional(readOnly = true)
  public List<AppliedMentoringInfo> loadAppliedMentoringList(SessionUser sessionUser) {

    User loginUser = userRepository.findByEmail(sessionUser.getEmail())
        .orElseThrow(
            () -> new NoSuchElementException(sessionUser.getEmail() + "에 해당하는 회원이 존재하지 않습니다."));

    List<MentoringApplication> mentoringApplicationList = loginUser.getMentoringApplicationList();

    return mentoringApplicationList.isEmpty() ?
        null :
        mentoringApplicationList
            .stream()
            .map(AppliedMentoringInfo::new)
            .toList();
  }


  /**
   * 아직 결제기능을 개발하지 않아, 결제취소 프로세스를 추가하지 않았습니다.
   */
  @Transactional
  public boolean changeAppliedMentoringStatus(SessionUser sessionUser, AppliedMentoringStatus appliedMentoringStatus) {

    User findUser = userRepository.findByEmail(sessionUser.getEmail())
        .orElseThrow(() -> new NoSuchElementException(sessionUser.getEmail() + "에 해당하는 회원이 존재하지 않습니다."));

    List<MentoringApplication> mentoringApplicationList = findUser.getMentoringApplicationList();

    MentoringApplication findMentoringApplication = findMatchingMentoringApplication(
        appliedMentoringStatus, mentoringApplicationList);

    MentoringStatus changeStatus = appliedMentoringStatus.getStatus();

    switch (changeStatus) {
      case CANCELED, COMPLETE -> {

        findMentoringApplication.changeMentoringStatus(changeStatus);

        deleteMentoringUnavailableTime(appliedMentoringStatus, findMentoringApplication);

        MentoringApplication modifiedMentoringStatus = mentoringApplicationRepository.save(
            findMentoringApplication);

        return modifiedMentoringStatus.isChangedMentoringStatus(changeStatus);
      }
      default -> throw new IllegalArgumentException("잘못된 상태변경입니다.");
    }
  }

  private MentoringApplication findMatchingMentoringApplication(AppliedMentoringStatus appliedMentoringStatus,
      List<MentoringApplication> mentoringApplicationList) {

    return mentoringApplicationList
        .stream()
        .filter(application ->
            application.isMatchingDateTime(appliedMentoringStatus))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("일치하는 멘토링 신청이력을 조회하지 못했습니다."));
  }


  private void deleteMentoringUnavailableTime(AppliedMentoringStatus appliedMentoringStatus,
      MentoringApplication mentoringApplication) {

    LocalDateTime targetStartDateTime = appliedMentoringStatus.getStartDateTime();
    LocalDateTime targetEndDateTime = appliedMentoringStatus.getEndDateTime();

    List<MentoringUnavailableTime> mentoringUnavailableTime = mentoringApplication.getMentoring()
        .getMentor()
        .getMentoringUnavailableTime();

    for (MentoringUnavailableTime unavailableTime : mentoringUnavailableTime) {

      LocalDateTime fromDateTime = unavailableTime.getFromDateTime();
      LocalDateTime toDateTime = unavailableTime.getToDateTime();

      if (fromDateTime.isEqual(targetStartDateTime) && toDateTime.isEqual(targetEndDateTime)) {

        mentoringUnavailableTimeRepository.delete(unavailableTime);
      }
    }
  }
}
