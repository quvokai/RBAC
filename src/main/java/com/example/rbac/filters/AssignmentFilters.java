package com.example.rbac.filters;

import com.example.rbac.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentFilters {

    public static AssignmentFilter byUser(User user) {
        return a -> a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String dateStr) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime after = LocalDateTime.parse(dateStr, fmt);
        return a -> {
            LocalDateTime assigned = LocalDateTime.parse(a.metadata().assignedAt(), fmt);
            return assigned.isAfter(after);
        };
    }

    public static AssignmentFilter expiringBefore(String dateStr) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime before = LocalDateTime.parse(dateStr, fmt);
        return a -> {
            if (!"TEMPORARY".equals(a.assignmentType())) return false;
            TemporaryAssignment ta = (TemporaryAssignment) a;
            LocalDateTime expires = LocalDateTime.parse(ta.getExpiresAt(), fmt);
            return expires.isBefore(before);
        };
    }
}