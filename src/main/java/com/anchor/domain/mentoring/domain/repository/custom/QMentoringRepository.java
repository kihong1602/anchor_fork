package com.anchor.domain.mentoring.domain.repository.custom;

import com.anchor.domain.mentoring.api.service.response.MentoringSearchResult;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QMentoringRepository {

  Page<MentoringSearchResult> findMentorings(List<String> tags, String keyword, Pageable pageable);

}