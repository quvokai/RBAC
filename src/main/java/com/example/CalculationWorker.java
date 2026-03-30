package com.example;

public class CalculationWorker implements Runnable {
    private final int threadOrderNumber;
    private final int totalSteps;
    private final long delayMs;

    public CalculationWorker(int threadOrderNumber, int totalSteps, long delayMs) {
        this.threadOrderNumber = threadOrderNumber;
        this.totalSteps = totalSteps;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();

        try {
            for (int i = 0; i <= totalSteps; i++) {
                Thread.sleep(delayMs);
                String progressBar = createProgressBar(i, totalSteps);
                String statusLine = String.format(
                        "Поток #%d | ID: %d | %s", 
                        threadOrderNumber, threadId, progressBar);
                ConsolePrinter.printLine(threadOrderNumber, statusLine);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String finalLine = String.format(
                "Поток #%d | ID: %d | [ЗАВЕРШЕНО] Время: %d мс", 
                threadOrderNumber, threadId, duration);
        ConsolePrinter.printLine(threadOrderNumber, finalLine);
    }

    public String createProgressBar(int current, int total) {
        int width = 30;
        int progress = (int) ((double) current / total * width);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            sb.append(i < progress ? "=" : " ");
        }
        sb.append("] ").append((int) ((double) current / total * 100)).append("%");
        return sb.toString();
    }
}
