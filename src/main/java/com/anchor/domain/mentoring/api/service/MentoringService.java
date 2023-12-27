package com.anchor.domain.mentoring.api.service;

import com.anchor.domain.mentor.domain.Mentor;
import com.anchor.domain.mentor.domain.repository.MentorRepository;
import com.anchor.domain.mentoring.api.controller.request.MentoringBasicInfo;
import com.anchor.domain.mentoring.api.controller.request.MentoringContentsInfo;
import com.anchor.domain.mentoring.api.service.response.MentoringContents;
import com.anchor.domain.mentoring.api.service.response.MentoringContentsEditResult;
import com.anchor.domain.mentoring.api.service.response.MentoringCreateResult;
import com.anchor.domain.mentoring.api.service.response.MentoringDeleteResult;
import com.anchor.domain.mentoring.api.service.response.MentoringDetailResponseDto;
import com.anchor.domain.mentoring.api.service.response.MentoringEditResult;
import com.anchor.domain.mentoring.api.service.response.MentoringResponseDto;
import com.anchor.domain.mentoring.domain.Mentoring;
import com.anchor.domain.mentoring.domain.repository.MentoringRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MentoringService {

  private final MentoringRepository mentoringRepository;
  private final MentorRepository mentorRepository;

  @Transactional
  public MentoringCreateResult create(Long mentorId, MentoringBasicInfo mentoringBasicInfo) {
    Mentor mentor = getMentorById(mentorId);
    Mentoring mentoring = Mentoring.createMentoring(mentor, mentoringBasicInfo);
    Mentoring savedMentoring = mentoringRepository.save(mentoring);
    return new MentoringCreateResult(savedMentoring.getId());
  }

  @Transactional
  public MentoringEditResult edit(Long id, MentoringBasicInfo mentoringBasicInfo) {
    Mentoring mentoring = getMentoringById(id);
    mentoring.changeBasicInfo(mentoringBasicInfo);
    Mentoring savedMentoring = mentoringRepository.save(mentoring);
    return new MentoringEditResult(savedMentoring.getId());
  }

  @Transactional
  public MentoringDeleteResult delete(Long id) {
    Mentoring mentoring = getMentoringById(id);
    mentoringRepository.delete(mentoring);
    return new MentoringDeleteResult(mentoring.getId());
  }

  @Transactional
  public MentoringContentsEditResult editContents(Long id, MentoringContentsInfo mentoringContentsInfo) {
    Mentoring mentoring = getMentoringById(id);
    mentoring.editContents(mentoringContentsInfo);
    Mentoring savedMentoring = mentoringRepository.save(mentoring);
    return new MentoringContentsEditResult(savedMentoring.getId());
  }

  @Transactional
  public MentoringContents getContents(Long id) {
    Mentoring mentoring = getMentoringById(id);
    return new MentoringContents(mentoring.getContents(), mentoring.getTags());
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public List<MentoringResponseDto> loadMentoringList() {
    List<Mentoring> mentoringList = mentoringRepository.findAll();

    return mentoringList.stream()
                        .map(MentoringResponseDto::new)
                        .toList();
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public MentoringDetailResponseDto loadMentoringDetail(Long id) {
    Mentoring findMentoring = mentoringRepository.findById(id)
                                                 .orElseThrow(() -> new NoSuchElementException(id + "에 해당하는 멘토링이 존재하지 않습니다."));
    return new MentoringDetailResponseDto(findMentoring);
  }

  private Mentoring getMentoringById(Long id) {
    return mentoringRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("일치하는 멘토링이 없습니다."));
  }

  private Mentor getMentorById(Long mentorId) {
    Mentor mentor = mentorRepository.findById(mentorId)
        .orElseThrow(() -> new NoSuchElementException("일치하는 멘토가 없습니다."));
    return mentor;
  }

  public List<MentoringUnavailableTimeDto> loadMentoringUnavailableTime(Long id) {
    Mentoring findMentoring = mentoringRepository.findById(id)
                                                 .orElseThrow(() -> new NoSuchElementException(id + "에 해당하는 멘토링이 존재하지 않습니다."));

    return findMentoring.getMentor()
                        .getMentoringUnavailableTime()
                        .isEmpty() ?
        null :
        new ArrayList<>(
            findMentoring.getMentor()
                         .getMentoringUnavailableTime()
                         .stream()
                         .map(MentoringUnavailableTimeDto::new)
                         .toList()
        );
  }
}
