package com.unison.practicas.desarrollo.library.service;

import com.unison.practicas.desarrollo.library.dto.UserPreview;
import com.unison.practicas.desarrollo.library.dto.UserPreviewsQuery;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final GetUsersPreviews getUsersPreviews;

    public UserService(GetUsersPreviews getUsersPreviews) {
        this.getUsersPreviews = getUsersPreviews;
    }

    public PaginationResponse<UserPreview> getUsersPreviews(UserPreviewsQuery query, PaginationRequest pagination) {
        return getUsersPreviews.handle(query, pagination);
    }

}
