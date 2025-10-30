package com.fernandoschilder.ipaconsolebackend.repository.spec;

import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<UserEntity> usernameContains(String q) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("username")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<UserEntity> enabledEquals(boolean enabled) {
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<UserEntity> hasRole(String roleName) {
        return (root, query, cb) -> {
            query.distinct(true); // evita duplicados por el join
            Join<UserEntity, RoleEntity> join = root.join("roles", JoinType.LEFT);
            return cb.equal(cb.lower(join.get("name")), roleName.toLowerCase());
        };
    }
}
