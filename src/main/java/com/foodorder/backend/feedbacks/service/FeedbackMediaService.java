package com.foodorder.backend.feedbacks.service;

import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaListResponse;
import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;

public interface FeedbackMediaService {
    FeedbackMediaListResponse getAll();
    FeedbackMediaResponse getById(Long id);
    FeedbackMediaResponse create(FeedbackMediaRequest request);
    FeedbackMediaResponse update(Long id, FeedbackMediaRequest request);
    void delete(Long id);
}
