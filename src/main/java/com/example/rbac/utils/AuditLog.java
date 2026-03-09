package com.example.rbac.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {

    private final List<AuditEntry> entries = new ArrayList<>();

    public static record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {}

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entries.add(new AuditEntry(timestamp, action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equals(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equals(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Журнал аудита пуст.");
            return;
        }
        System.out.println("Журнал аудита:");
        System.out.println("------------------------------------------------------------");
        for (AuditEntry e : entries) {
            System.out.printf("%s | %-15s | %-10s | %-20s | %s%n",
                    e.timestamp(), e.action(), e.performer(), e.target(), e.details());
        }
        System.out.println("------------------------------------------------------------");
    }
}