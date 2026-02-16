package com.example.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(
    String assignedBy,
    String assignedAt,
    String reason  
) {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedBy не может быть пустым");
        }
        String nowStr = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy.trim(), nowStr, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Назначил: ").append(assignedBy)
          .append(" в ").append(assignedAt);
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(" | Причина: ").append(reason.trim());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        AssignmentMetadata md1 = AssignmentMetadata.now("admin", "Первоначальное назначение");
        System.out.println(md1.format());

        AssignmentMetadata md2 = AssignmentMetadata.now("system", null);
        System.out.println(md2.format());

     
        // AssignmentMetadata.now("", "test"); 
    }
}