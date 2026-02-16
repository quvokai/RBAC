package com.example.rbac;

import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {

    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role не может быть null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata не может быть null");
        }
        
        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public String summary() {
        String base = "[" + assignmentType() + "] " + role.getName() +
                      " assigned to " + user.username() +
                      " by " + metadata.assignedBy() +
                      " at " + metadata.assignedAt();

        if (metadata.reason() != null && !metadata.reason().isEmpty()) {
            base += " Reason: " + metadata.reason();
        }

        return base;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return assignmentId.equals(that.assignmentId);
    }

    @Override
    public int hashCode() {
        return assignmentId.hashCode();
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();
}