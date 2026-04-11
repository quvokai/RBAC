package com.example.rbac.utils;
import java.util.concurrent.*;

public class BackgroundExecutor {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Конструктор public, чтобы работало new BackgroundExecutor() в RBACSystem
    public BackgroundExecutor() {}

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    // Алиас для Runnable, вызывается в registerUtilityCommands
    public Future<?> submitAsync(Runnable task) {
        return executor.submit(task);
    }

    public void awaitAndPrint(Future<String> future, String taskName, long timeoutMillis) {
        executor.submit(() -> {
            try {
                String result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                System.out.println("\n[ASYNC RESULT] " + taskName + ":\n" + result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[ASYNC] Задача прервана: " + taskName);
            } catch (ExecutionException e) {
                System.out.println("[ASYNC] Ошибка выполнения " + taskName + ": " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                System.out.println("[ASYNC] Превышено время ожидания для: " + taskName);
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}