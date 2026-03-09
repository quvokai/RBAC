package com.example.rbac.commands;

import com.example.rbac.system.RBACSystem;
import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system);
}
