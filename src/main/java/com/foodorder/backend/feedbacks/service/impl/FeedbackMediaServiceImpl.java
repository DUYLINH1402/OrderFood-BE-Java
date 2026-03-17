package com.foodorder.backend.feedbacks.service.impl;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaListResponse;
import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;
import com.foodorder.backend.feedbacks.entity.FeedbackMedia;
import com.foodorder.backend.feedbacks.repository.FeedbackMediaRepository;
import com.foodorder.backend.feedbacks.service.FeedbackMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.foodorder.backend.config.CacheConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackMediaServiceImpl implements FeedbackMediaService {

    private final FeedbackMediaRepository repo;

    @Override
    @Cacheable(value = FEEDBACK_MEDIA_LIST_CACHE, key = "'all'")
    public FeedbackMediaListResponse getAll() {
        List<FeedbackMediaResponse> items = repo.findAll(Sort.by("displayOrder"))
                .stream().map(this::toResponse).toList();
        return FeedbackMediaListResponse.of(items);
    }

    @Override
    @Cacheable(value = FEEDBACK_MEDIA_DETAIL_CACHE, key = "#id")
    public FeedbackMediaResponse getById(Long id) {
        FeedbackMedia media = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback media not found", "FEEDBACK_MEDIA_NOT_FOUND"));
        return toResponse(media);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = FEEDBACK_MEDIA_LIST_CACHE, allEntries = true),
            @CacheEvict(value = FEEDBACK_MEDIA_DETAIL_CACHE, allEntries = true)
    })
    public FeedbackMediaResponse create(FeedbackMediaRequest req) {
        FeedbackMedia media = new FeedbackMedia();
        media.setType(FeedbackMedia.MediaType.valueOf(req.getType()));
        media.setMediaUrl(req.getMediaUrl());
        media.setThumbnailUrl(req.getThumbnailUrl());
        media.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        repo.save(media);
        return toResponse(media);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = FEEDBACK_MEDIA_LIST_CACHE, allEntries = true),
            @CacheEvict(value = FEEDBACK_MEDIA_DETAIL_CACHE, allEntries = true)
    })
    public FeedbackMediaResponse update(Long id, FeedbackMediaRequest req) {
        FeedbackMedia media = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback media not found", "FEEDBACK_MEDIA_NOT_FOUND"));
        media.setType(FeedbackMedia.MediaType.valueOf(req.getType()));
        media.setMediaUrl(req.getMediaUrl());
        media.setThumbnailUrl(req.getThumbnailUrl());
        media.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        repo.save(media);
        return toResponse(media);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = FEEDBACK_MEDIA_LIST_CACHE, allEntries = true),
            @CacheEvict(value = FEEDBACK_MEDIA_DETAIL_CACHE, allEntries = true)
    })
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private FeedbackMediaResponse toResponse(FeedbackMedia media) {
        FeedbackMediaResponse resp = new FeedbackMediaResponse();
        resp.setId(media.getId());
        resp.setType(media.getType().name());
        resp.setMediaUrl(media.getMediaUrl());
        resp.setThumbnailUrl(media.getThumbnailUrl());
        resp.setDisplayOrder(media.getDisplayOrder());
        resp.setCreatedAt(media.getCreatedAt());
        return resp;
    }
}


