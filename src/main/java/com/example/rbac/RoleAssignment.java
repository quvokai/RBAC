package com.example.rbac;

public interface RoleAssignment {

    String assignmentId();

    User user();

    Role role();

    AssignmentMetadata metadata();

    boolean isActive();

    String assignmentType();

    default String summary() {
        return "[" + assignmentType() + "] " + role().getName() +
               " assigned to " + user().username() +
               " by " + metadata().assignedBy() +
               " at " + metadata().assignedAt() +
               (metadata().reason() != null && !metadata().reason().isEmpty() ?
                   " Reason: " + metadata().reason() : "");
    }
}