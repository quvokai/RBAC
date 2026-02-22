package com.example.rbac.managers;

import com.example.rbac.filters.AssignmentFilters;
import com.example.rbac.*;
import com.example.rbac.filters.AssignmentFilter;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignments = new HashMap<>();  // ключ — assignmentId

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) throw new IllegalArgumentException("Назначение не может быть null");
        if (assignments.containsKey(assignment.assignmentId())) {
            throw new IllegalArgumentException("Назначение с таким ID уже существует");
        }

        // Проверка: одна активная роль не может быть назначена дважды
        boolean duplicateActive = assignments.values().stream()
                .filter(a -> a.user().equals(assignment.user()))
                .filter(RoleAssignment::isActive)
                .anyMatch(a -> a.role().equals(assignment.role()));

        if (duplicateActive) throw new IllegalArgumentException("Активное назначение этой роли пользователю уже существует");

        assignments.put(assignment.assignmentId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;
        return assignments.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) return findAll();
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        if (sorter != null) result.sort(sorter);
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return findByFilter(AssignmentFilters.inactiveOnly());
    }

    public boolean userHasRole(User user, Role role) {
        return findByUser(user).stream()
                .anyMatch(a -> a.role().equals(role) && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) && p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> perms = new HashSet<>();
        findByUser(user).stream()
                .filter(RoleAssignment::isActive)
                .map(RoleAssignment::role)
                .forEach(role -> perms.addAll(role.getPermissions()));
        return perms;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) throw new IllegalArgumentException("Назначение не найдено");
        if (assignment instanceof PermanentAssignment) {
            ((PermanentAssignment) assignment).revoke();
        } else if (assignment instanceof TemporaryAssignment) {
            // Для временного — делаем expired (можно установить дату в прошлом)
            ((TemporaryAssignment) assignment).extend("2000-01-01 00:00");
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) throw new IllegalArgumentException("Назначение не найдено");
        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Это не временное назначение");
        }
        ((TemporaryAssignment) assignment).extend(newExpirationDate);
    }
}
