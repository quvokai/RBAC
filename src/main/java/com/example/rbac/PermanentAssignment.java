package com.example.rbac;

public class PermanentAssignment extends AbstractRoleAssignment {

    private boolean revoked = false;

    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }

    @Override
    public boolean isActive() {
        return !revoked;
    }

    @Override
    public String assignmentType() {
        return "PERMANENT";
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isRevoked() {
        return revoked;
    }

    @Override
    public String summary() {
        String base = super.summary();
        if (revoked) {
            base += " (REVOKED)";
        }
        return base;
    }

    public static void main(String[] args) {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        Role admin = new Role("Administrator", "Полный доступ");
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial setup");

        PermanentAssignment assignment = new PermanentAssignment(user, admin, meta);

        System.out.println(assignment.summary());
        System.out.println("Active? " + assignment.isActive());

        assignment.revoke();
        System.out.println("После revoke:");
        System.out.println(assignment.summary());
        System.out.println("Active? " + assignment.isActive());
        System.out.println("Revoked? " + assignment.isRevoked());
    }
}