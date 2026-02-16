package com.example.rbac;

import java.util.*;

public class Role {

    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions = new HashSet<>();

    public Role(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название роли не может быть пустым");
        }
        this.id = "role_" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name.trim();
        this.description = (description != null) ? description.trim() : "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission не может быть null");
        }
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                               p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name)
          .append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description.isEmpty() ? "— нет описания —" : description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");

        if (permissions.isEmpty()) {
            sb.append("  (пусто)\n");
        } else {
            for (Permission p : permissions) {
                sb.append("  - ").append(p.format()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Role{id='" + id + "', name='" + name + "'}";
    }

    public static void main(String[] args) {
        Role admin = new Role("Administrator", "Полный доступ к системе");

        admin.addPermission(new Permission("READ", "users", "Просмотр пользователей"));
        admin.addPermission(new Permission("WRITE", "users", "Создание/редактирование пользователей"));
        admin.addPermission(new Permission("DELETE", "users", "Удаление пользователей"));

        System.out.println(admin.format());

        System.out.println("Has READ on users? " + admin.hasPermission("read", "users"));
        System.out.println("Has DELETE on reports? " + admin.hasPermission("delete", "reports"));
    }
}