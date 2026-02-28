package com.example.rbac.sorters;

import com.example.rbac.RoleAssignment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class AssignmentSorters {

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(a -> a.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(a -> a.role().getName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return Comparator.comparing(a -> LocalDateTime.parse(a.metadata().assignedAt(), fmt));
    }
}
*