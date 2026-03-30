package com.example;

public class ConsolePrinter {
    private static final Object LOCK = new Object();
    private static int maxLines = 0;

    public static void printLine(int lineIndex, String content) {
        synchronized (LOCK) {
            if (lineIndex >= maxLines) {
                System.out.println(content);
                maxLines++;
            } else {
                System.out.print("\r\033[K" + content);
                if (lineIndex < maxLines - 1) {
                    System.out.print("\033[" + (maxLines - 1 - lineIndex) + "B");
                }
            }
            System.out.flush();
        }
    }
}

