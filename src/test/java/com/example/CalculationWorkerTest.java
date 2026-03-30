package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculationWorkerTest {

    @Test
    public void testProgressBar_Start() {
        CalculationWorker worker = new CalculationWorker(0, 100, 10);
        String bar = worker.createProgressBar(0, 100);
        assertTrue(bar.startsWith("["));
        assertTrue(bar.contains("0%"));
    }

    @Test
    public void testProgressBar_Middle() {
        CalculationWorker worker = new CalculationWorker(0, 100, 10);
        String bar = worker.createProgressBar(50, 100);
        assertTrue(bar.contains("50%"));
        assertTrue(bar.contains("="));
    }

    @Test
    public void testProgressBar_Finish() {
        CalculationWorker worker = new CalculationWorker(0, 100, 10);
        String bar = worker.createProgressBar(100, 100);
        assertTrue(bar.contains("100%"));
        assertTrue(bar.endsWith("%"));
    }

    @Test
    public void testWorkerInitialization() {
        CalculationWorker worker = new CalculationWorker(5, 200, 50);
        assertNotNull(worker);
    }
}

