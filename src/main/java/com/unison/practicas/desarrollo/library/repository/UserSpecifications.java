package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.model.Role;
import com.unison.practicas.desarrollo.library.model.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

public interface UserSpecifications {

    static Specification<User> withSearch(String search) {
        if (!StringUtils.hasText(search)) {
            return (root, query, cb) -> cb.conjunction();
        }

        String like = "%" + search.toLowerCase() + "%";

        return (root, query, cb) ->
            cb.or(
                cb.like(cb.lower(root.get("firstName")), like),
                cb.like(cb.lower(root.get("lastName")), like),
                cb.like(cb.lower(root.get("email")), like)
            );
    }

    static Specification<User> withAnyRole(Set<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            Join<User, Role> join = root.join("roles");
            return join.get("slug").in(roles);
        };
    }

    static Specification<User> becameMemberBetweenDates(LocalDate min, LocalDate max) {
        Instant start = min != null ? min.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant end   = max != null ? max.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("createdAt"), start, end);
            }

            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            }

            if (end != null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), end);
            }

            return cb.conjunction();
        };
    }

    static Specification<User> withActiveBookLoans(Integer min, Integer max) {
        return (root, query, cb) -> {
            return cb.conjunction();
        };
    }


}