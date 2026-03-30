package com.example;

public class App {
    // Конфигурируемые переменные
    private static final int THREAD_COUNT = 4;
    private static final int CALCULATION_STEPS = 50;
    private static final int STEP_DELAY_MS = 100;

    public static void main(String[] args) {
        System.out.println("Запуск имитации многопоточного расчёта...");
        System.out.println("Потоков: " + THREAD_COUNT + ", Шагов: " + CALCULATION_STEPS);
        System.out.println("--------------------------------------------------");
        System.out.println();

        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            CalculationWorker worker = new CalculationWorker(i, CALCULATION_STEPS, STEP_DELAY_MS);
            threads[i] = new Thread(worker, "Worker-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("Все расчёты завершены.");
    }
}
