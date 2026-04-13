package com.example.rbac.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuditLog {

    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final List<AuditEntry> entries = new ArrayList<>();
    private final Thread workerThread;
    private volatile boolean running = true;

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {}

    public AuditLog() {
        workerThread = new Thread(this::processQueue, "AuditLog-Worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        queue.offer(new AuditEntry(timestamp, action, performer, target, details));
    }

    private void processQueue() {
        while (running || !queue.isEmpty()) {
            try {
                AuditEntry entry = queue.take();
                synchronized (entries) {
                    entries.add(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.performer().equals(performer))
                    .toList();
        }
    }

    public List<AuditEntry> getByAction(String action) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.action().equals(action))
                    .toList();
        }
    }

    public void printLog() {
        synchronized (entries) {
            if (entries.isEmpty()) {
                System.out.println("Журнал аудита пуст.");
                return;
            }
            System.out.println("=== Журнал аудита ===");
            entries.forEach(e -> System.out.printf("%s | %-12s | %-10s | %s%n",
                    e.timestamp(), e.action(), e.performer(), e.details()));
        }
    }

    public void shutdown() {
        running = false;
        workerThread.interrupt();
        System.out.println("[AuditLog] Аудит-лог остановлен.");
    }
}