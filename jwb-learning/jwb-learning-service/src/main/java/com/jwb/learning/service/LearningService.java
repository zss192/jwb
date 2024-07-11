package com.jwb.learning.service;

import com.jwb.base.model.RestResponse;

public interface LearningService {
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
