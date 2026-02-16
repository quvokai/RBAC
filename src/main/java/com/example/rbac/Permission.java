package com.example.rbac;

public record Permission(String name, String resource, String description) {

    public Permission {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя права не может быть пустым");
        }
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Имя права не должно содержать пробелов");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }

       
        name = name.toUpperCase();
        resource = resource.toLowerCase();
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null || resourcePattern == null) return false;
        return name.contains(namePattern.toUpperCase()) &&
               resource.contains(resourcePattern.toLowerCase());
    }

    
    public static void main(String[] args) {
        try {
            Permission p = new Permission("read", "Users", "Просмотр списка пользователей");
            System.out.println(p.format());

            Permission p2 = new Permission("Write ", "reports ", "Редактирование отчётов");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        Permission p3 = new Permission("DELETE", "settings", "Удаление настроек");
        System.out.println(p3.format());

        System.out.println("Совпадает с 'del' и 'set': " + p3.matches("del", "set"));   
    }
}