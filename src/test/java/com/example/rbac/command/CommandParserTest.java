package com.example.rbac.commands;

import com.example.rbac.RBACSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
    }

    @Test
    void testRegisterAndExecuteCommand() {
        AtomicBoolean executed = new AtomicBoolean(false);

        parser.registerCommand("test", "Test description", (scanner, sys) -> executed.set(true));

        parser.parseAndExecute("test", new Scanner(""), system);
        
        assertTrue(executed.get(), "Команда должна быть выполнена");
    }

    @Test
    void testUnknownCommandDoesNotThrowException() {
        assertDoesNotThrow(() -> {
            parser.parseAndExecute("unknown_command", new Scanner(""), system);
        });
    }

    @Test
    void testCaseInsensitiveExecution() {
        AtomicBoolean executed = new AtomicBoolean(false);
        parser.registerCommand("HELLO", "Desc", (scanner, sys) -> executed.set(true));

        parser.parseAndExecute("hello", new Scanner(""), system);
        
        assertTrue(executed.get(), "Парсер должен игнорировать регистр названия команды");
    }
}