package com.example.rbac.utils;

import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();

        // Ширина столбцов
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        // Заголовок
        sb.append("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        sb.append("\n");

        sb.append("|");
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(String.format("%-" + widths[i] + "s", headers[i])).append(" |");
        }
        sb.append("\n");

        sb.append("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        sb.append("\n");

        // Строки
        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < row.length; i++) {
                sb.append(" ").append(String.format("%-" + widths[i] + "s", row[i])).append(" |");
            }
            sb.append("\n");
        }

        sb.append("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        sb.append("\n");

        return sb.toString();
    }

    public static String formatBox(String text) {
        int width = text.length() + 4;
        StringBuilder sb = new StringBuilder();
        sb.append("┌").append("─".repeat(width)).append("┐\n");
        sb.append("│  ").append(text).append("  │\n");
        sb.append("└").append("─".repeat(width)).append("┘\n");
        return sb.toString();
    }

    public static String formatHeader(String text) {
        return "=== " + text.toUpperCase() + " ===\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        return String.format("%-" + length + "s", text);
    }

    public static String padLeft(String text, int length) {
        return String.format("%" + length + "s", text);
    }
}
