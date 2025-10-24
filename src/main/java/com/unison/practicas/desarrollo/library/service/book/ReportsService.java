package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryPopularityGroupResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportsService {

    private final GetPopularBookCategories getPopularBookCategories;

    public ReportsService(GetPopularBookCategories getPopularBookCategories) {
        this.getPopularBookCategories = getPopularBookCategories;
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<BookCategoryPopularityGroupResponse> getPopularBookCategories() {
        return getPopularBookCategories.get();
    }

}
