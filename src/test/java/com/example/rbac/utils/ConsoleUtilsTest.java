package com.example.rbac.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Scanner;

public class ConsoleUtilsTest {

    @Test
    void testPromptString() {
        Scanner scanner = new Scanner("test input\n");
        String input = ConsoleUtils.promptString(scanner, "Введите строку", true);
        assertEquals("test input", input);
    }

}
