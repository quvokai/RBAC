package com.example.rbac.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command cmd = commands.get(commandName.toLowerCase());
        if (cmd != null) {
            cmd.execute(scanner, system);
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            printHelp();
        }
    }

    public void printHelp() {
        System.out.println("Доступные команды:");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("  %-15s - %s%n", entry.getKey(), entry.getValue());
        }
        System.out.println("\nДля справки введите 'help'");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) return;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        executeCommand(commandName, scanner, system);
    }
}
