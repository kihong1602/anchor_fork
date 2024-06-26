package com.anchor.global.redis.lock;

import com.anchor.domain.mentoring.domain.Mentoring;
import com.anchor.global.exception.type.entity.IncreaseCountFailedException;
import com.anchor.global.exception.type.redis.ApplicationTimeLockFailedException;
import com.anchor.global.exception.type.redis.LockAcquisitionFailedException;
import com.anchor.global.redis.client.ReservationTimeInfo;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisLockFacade {

  private final MentoringServiceWithLock mentoringServiceWithLock;
  private final RedissonClient redissonClient;

  public Mentoring increaseTotalApplication(Long mentoringId) {
    RLock lock = redissonClient.getLock(mentoringId.toString());

    try {
      boolean available = lock.tryLock(3, 1, TimeUnit.SECONDS);
      if (!available) {
        throw new LockAcquisitionFailedException();
      }
      return mentoringServiceWithLock.increaseTotalApplication(mentoringId);
    } catch (InterruptedException | LockAcquisitionFailedException e) {
      Thread.currentThread()
          .interrupt();
      log.error("[멘토링 번호 : {}] :: 신청자수 증가 락 획득 실패", mentoringId);
      throw new IncreaseCountFailedException(e);
    } finally {
      lock.unlock();
    }

  }

  public String lockApplicationTime(Long mentorId, ReservationTimeInfo reservationTimeInfo) {
    String formattedTime = reservationTimeInfo.getReservedTime()
        .formattingFromTime();
    String key = mentorId + ":" + formattedTime;
    RLock lock = redissonClient.getLock(key);
    try {
      boolean available = lock.tryLock(10, 5, TimeUnit.SECONDS);
      if (!available) {
        throw new LockAcquisitionFailedException();
      }
      return mentoringServiceWithLock.getLock(mentorId, reservationTimeInfo);
    } catch (InterruptedException | LockAcquisitionFailedException e) {
      Thread.currentThread()
          .interrupt();
      throw new ApplicationTimeLockFailedException(e);
    } finally {
      lock.unlock();
    }
  }

}

