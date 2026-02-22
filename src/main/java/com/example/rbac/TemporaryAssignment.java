package com.example.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class TemporaryAssignment extends AbstractRoleAssignment {

    private String expiresAt;       
    private final boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("expiresAt не может быть пустым");
        }
        this.expiresAt = expiresAt.trim();
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return isActive(LocalDateTime.now());
    }


    public boolean isActive(LocalDateTime currentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime expiration = LocalDateTime.parse(expiresAt, formatter);

        if (currentTime.isBefore(expiration)) {
            return true;
        }

        if (autoRenew) {

            expiration = expiration.plusDays(7);
            expiresAt = expiration.format(formatter);
            return true;
        }

        return false;
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpiresAt) {
        if (newExpiresAt == null || newExpiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Новая дата не может быть пустой");
        }
        this.expiresAt = newExpiresAt.trim();
    }

    public boolean isExpired() {
        return !isActive();
    }

    public String getTimeRemaining() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime expiration = LocalDateTime.parse(expiresAt, formatter);
        Duration remaining = Duration.between(LocalDateTime.now(), expiration);

        if (remaining.isNegative()) {
            return "Истёк";
        }

        long days = remaining.toDays();
        long hours = remaining.toHoursPart();
        return days + " дн. " + hours + " ч. осталось";
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String summary() {
        return super.summary() + " | Expires: " + expiresAt + 
               (autoRenew ? " (auto-renew)" : "");
    }


    public static void main(String[] args) {
        User user = User.create("alice", "Alice Smith", "alice@example.com");
        Role viewer = new Role("Viewer", "Только просмотр");
        AssignmentMetadata meta = AssignmentMetadata.now("manager", "Временный доступ к отчётам");

        LocalDateTime now = LocalDateTime.now();
        String expires = now.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TemporaryAssignment temp = new TemporaryAssignment(user, viewer, meta, expires, true);

        System.out.println(temp.summary());
        System.out.println("Active? " + temp.isActive());
        System.out.println("Time remaining: " + temp.getTimeRemaining());

        temp.extend(now.plusDays(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("После продления:");
        System.out.println(temp.summary());
    }
}