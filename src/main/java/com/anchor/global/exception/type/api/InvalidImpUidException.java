package com.anchor.global.exception.type.api;

import com.anchor.global.exception.ExternalApiException;
import com.anchor.global.exception.error.AnchorErrorCode;
import com.anchor.global.exception.response.SimpleErrorDetail;

public class InvalidImpUidException extends ExternalApiException {

  public InvalidImpUidException(String message) {
    super(AnchorErrorCode.INVALID_IMP_UID, new SimpleErrorDetail(message));
  }
}
