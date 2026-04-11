package com.example.rbac.managers;

import com.example.rbac.Role;
import com.example.rbac.Permission;
import com.example.rbac.filters.RoleFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {

    private final ConcurrentHashMap<String, Role> rolesById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Role> rolesByName = new ConcurrentHashMap<>();

    @Override
    public void add(Role role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }
        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;
        rolesById.remove(role.getId());
        return rolesByName.remove(role.getName()) != null;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) return findAll();
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) result.sort(sorter);
        return result;
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) throw new IllegalArgumentException("Role not found");
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) throw new IllegalArgumentException("Role not found");
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(r -> r.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }
}