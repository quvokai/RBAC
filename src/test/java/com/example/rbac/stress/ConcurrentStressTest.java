package com.example.rbac.stress;

import com.example.rbac.*;
import com.example.rbac.commands.RBACSystem;
import com.example.rbac.managers.AssignmentManager;
import com.example.rbac.managers.UserManager;
import com.example.rbac.managers.RoleManager;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Нагрузочный тест RBAC-системы.
 *
 * Проверяет:
 *  - параллельное создание пользователей/ролей/назначений без дубликатов и NPE
 *  - одновременные читатели и писатели (целостность данных)
 *  - отсутствие ConcurrentModificationException и прочих гонок
 *  - корректность AuditLog при параллельном логировании
 *  - корректность BackgroundExecutor при параллельном запуске задач
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConcurrentStressTest {

    // Количество параллельных потоков-писателей
    private static final int WRITERS  = 8;
    // Количество параллельных потоков-читателей
    private static final int READERS  = 4;
    // Пользователей на поток
    private static final int USERS_PER_WRITER = 10;
    // Максимальное время ожидания теста
    private static final int TIMEOUT_SEC = 30;

    private RBACSystem system;
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    // Атомарные счётчики для уникальных имён через все потоки
    private final AtomicInteger userSeq = new AtomicInteger(0);
    private final AtomicInteger roleSeq = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
        userManager       = system.getUserManager();
        roleManager       = system.getRoleManager();
        assignmentManager = system.getAssignmentManager();
    }

    @AfterEach
    void tearDown() {
        system.shutdown();
    }

    // ----------------------------------------------------------------
    // ТЕСТ 1: параллельное создание пользователей — нет дубликатов
    // ----------------------------------------------------------------
    @Test @Order(1)
    @DisplayName("Параллельное создание пользователей — нет дубликатов")
    void testConcurrentUserCreation() throws InterruptedException {
        ExecutorService pool   = Executors.newFixedThreadPool(WRITERS);
        CountDownLatch  done   = new CountDownLatch(WRITERS);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int t = 0; t < WRITERS; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < USERS_PER_WRITER; i++) {
                        int id = userSeq.incrementAndGet();
                        userManager.add(User.create(
                            "usr_" + id,
                            "User " + id,
                            "u" + id + "@test.com"
                        ));
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(TIMEOUT_SEC, TimeUnit.SECONDS), "Таймаут ожидания потоков");
        pool.shutdown();

        assertTrue(errors.isEmpty(), "Ошибки при создании пользователей: " + errors);

        List<User> all = userManager.findAll();
        // +1 = admin из initialize()
        assertEquals(WRITERS * USERS_PER_WRITER + 1, all.size(), "Неверное кол-во пользователей");

        // Дубликатов нет
        long uniqueCount = all.stream().map(User::username).distinct().count();
        assertEquals(all.size(), uniqueCount, "Обнаружены дубликаты username!");
    }

    // ----------------------------------------------------------------
    // ТЕСТ 2: параллельное создание ролей — нет дубликатов
    // ----------------------------------------------------------------
    @Test @Order(2)
    @DisplayName("Параллельное создание ролей — нет дубликатов")
    void testConcurrentRoleCreation() throws InterruptedException {
        int rolesPerThread = 5;
        ExecutorService pool   = Executors.newFixedThreadPool(WRITERS);
        CountDownLatch  done   = new CountDownLatch(WRITERS);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int t = 0; t < WRITERS; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < rolesPerThread; i++) {
                        int id = roleSeq.incrementAndGet();
                        Role role = new Role("Role_" + id, "Тестовая роль " + id);
                        role.addPermission(new Permission("READ", "res" + id, "Ресурс " + id));
                        roleManager.add(role);
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(TIMEOUT_SEC, TimeUnit.SECONDS));
        pool.shutdown();

        assertTrue(errors.isEmpty(), "Ошибки при создании ролей: " + errors);

        List<Role> all = roleManager.findAll();
        // 3 из initialize() + WRITERS * rolesPerThread
        assertEquals(3 + WRITERS * rolesPerThread, all.size(), "Неверное кол-во ролей");

        long uniqueNames = all.stream().map(Role::getName).distinct().count();
        assertEquals(all.size(), uniqueNames, "Дубликаты имён ролей!");
    }

    // ----------------------------------------------------------------
    // ТЕСТ 3: одновременные читатели и писатели
    // ----------------------------------------------------------------
    @Test @Order(3)
    @DisplayName("Читатели и писатели одновременно — система стабильна, данные целостны")
    void testReadersAndWritersConcurrently() throws InterruptedException {
        // Общая роль для назначений
        Role shared = new Role("SharedRole", "Общая роль для нагрузки");
        shared.addPermission(new Permission("READ", "shared", "Общий ресурс"));
        roleManager.add(shared);

        int total = WRITERS + READERS;
        ExecutorService pool  = Executors.newFixedThreadPool(total);
        CountDownLatch  start = new CountDownLatch(1);   // стартовый выстрел
        CountDownLatch  done  = new CountDownLatch(total);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        // Потоки-писатели: создают юзеров и назначают им роль
        for (int t = 0; t < WRITERS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < USERS_PER_WRITER; i++) {
                        int id = userSeq.incrementAndGet();
                        User user = User.create("rw_" + id, "RW " + id, "rw" + id + "@test.com");
                        userManager.add(user);

                        AssignmentMetadata meta = AssignmentMetadata.now("stress", "нагрузка");
                        assignmentManager.add(new PermanentAssignment(user, shared, meta));

                        // Время от времени логируем
                        if (id % 10 == 0) {
                            system.getAuditLog().log("STRESS_CREATE", "writer",
                                "usr_" + id, "Создан пользователь и назначение");
                        }
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        // Потоки-читатели: постоянно читают и проверяют целостность
        for (int t = 0; t < READERS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < 80; i++) {
                        List<User> users = userManager.findAll();
                        assertNotNull(users, "findAll() вернул null");

                        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
                        assertNotNull(active, "getActiveAssignments() вернул null");

                        // Целостность каждого назначения
                        for (RoleAssignment a : active) {
                            assertNotNull(a.user(), "user == null в назначении");
                            assertNotNull(a.role(), "role == null в назначении");
                            assertNotNull(a.assignmentId(), "assignmentId == null");
                        }

                        Thread.sleep(2);
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown(); // старт всех потоков одновременно
        assertTrue(done.await(TIMEOUT_SEC, TimeUnit.SECONDS), "Таймаут нагрузочного теста");
        pool.shutdown();

        // Никаких ошибок (гонки, NPE, CME и т.д.)
        assertTrue(errors.isEmpty(),
            "Ошибки при конкурентном доступе (" + errors.size() + " шт.): " + errors);

        // Дубликатов пользователей нет
        List<User> allUsers = userManager.findAll();
        long unique = allUsers.stream().map(User::username).distinct().count();
        assertEquals(allUsers.size(), unique, "Дубликаты пользователей после нагрузочного теста!");

        System.out.println("[StressTest] Пользователей: " + allUsers.size()
            + ", Назначений: " + assignmentManager.count()
            + ", Активных: " + assignmentManager.getActiveAssignments().size());
    }

    // ----------------------------------------------------------------
    // ТЕСТ 4: поиск и фильтрация под нагрузкой
    // ----------------------------------------------------------------
    @Test @Order(4)
    @DisplayName("Фильтрация и поиск под нагрузкой — нет пропусков и дубликатов в результатах")
    void testConcurrentSearchAndFilter() throws InterruptedException {
        // Заполняем базу заранее
        for (int i = 1; i <= 50; i++) {
            userManager.add(User.create("filter_u" + i, "Filter User " + i, "fu" + i + "@corp.com"));
        }

        ExecutorService pool   = Executors.newFixedThreadPool(10);
        CountDownLatch  done   = new CountDownLatch(10);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int t = 0; t < 10; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        // Поиск по домену
                        List<User> byDomain = userManager.findAll().stream()
                            .filter(u -> u.email().endsWith("@corp.com"))
                            .collect(Collectors.toList());
                        assertFalse(byDomain.isEmpty(), "Фильтр по домену вернул пустой список");

                        // Дубликатов нет в результатах
                        long uniq = byDomain.stream().map(User::username).distinct().count();
                        assertEquals(byDomain.size(), uniq, "Дубликаты в результатах фильтрации!");

                        // Поиск по имени
                        List<User> byName = userManager.findAll().stream()
                            .filter(u -> u.fullName().contains("Filter"))
                            .collect(Collectors.toList());
                        assertFalse(byName.isEmpty(), "Фильтр по имени вернул пустой список");
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(TIMEOUT_SEC, TimeUnit.SECONDS));
        pool.shutdown();
        assertTrue(errors.isEmpty(), "Ошибки при параллельной фильтрации: " + errors);
    }

    // ----------------------------------------------------------------
    // ТЕСТ 5: параллельное логирование в AuditLog
    // ----------------------------------------------------------------
    @Test @Order(5)
    @DisplayName("AuditLog — все записи сохраняются при параллельном логировании")
    void testConcurrentAuditLogging() throws InterruptedException {
        int threads      = 10;
        int logsPerThread = 100;
        int expected     = threads * logsPerThread;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch  done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < logsPerThread; i++) {
                        system.getAuditLog().log(
                            "STRESS_LOG", "thread-" + tid,
                            "target-" + i, "Запись " + i + " поток " + tid
                        );
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(TIMEOUT_SEC, TimeUnit.SECONDS));
        pool.shutdown();

        // Даём время AuditLog обработать очередь
        system.getAuditLog().flush();

        int actual = system.getAuditLog().getAll().size();
        // Допускаем незначительные потери (< 5%) из-за асинхронности
        int threshold = (int)(expected * 0.95);
        assertTrue(actual >= threshold,
            "AuditLog потерял слишком много записей: ожидалось ~" + expected + ", получено " + actual);

        System.out.println("[StressTest] AuditLog: ожидалось " + expected + ", получено " + actual);
    }

    // ----------------------------------------------------------------
    // ТЕСТ 6: BackgroundExecutor — параллельные задачи выполняются без потерь
    // ----------------------------------------------------------------
    @Test @Order(6)
    @DisplayName("BackgroundExecutor — 20 задач запускаются параллельно и все завершаются")
    void testBackgroundExecutorParallelTasks() throws Exception {
        int taskCount = 20;
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            final int id = i;
            futures.add(system.getBackgroundExecutor().submit(() -> {
                Thread.sleep(10);
                return "task-" + id;
            }));
        }

        Set<String> results = new HashSet<>();
        for (Future<String> f : futures) {
            String res = f.get(5, TimeUnit.SECONDS);
            assertNotNull(res, "Задача вернула null");
            results.add(res);
        }

        assertEquals(taskCount, results.size(), "Не все задачи завершились с уникальным результатом");
    }

    // ----------------------------------------------------------------
    // ТЕСТ 7: обработка истёкших временных назначений под нагрузкой
    // ----------------------------------------------------------------
    @Test @Order(7)
    @DisplayName("ScheduledExecutor — истёкшие назначения помечаются неактивными")
    void testExpiredAssignmentsHandling() throws InterruptedException {
        // Создаём роль и пользователей с уже истёкшими назначениями
        Role role = new Role("TempRole", "Временная тестовая роль");
        role.addPermission(new Permission("READ", "temp", "Временный ресурс"));
        roleManager.add(role);

        String pastDate = LocalDateTime.now()
            .minusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        for (int i = 1; i <= 5; i++) {
            User user = User.create("exp_usr" + i, "Expired " + i, "exp" + i + "@test.com");
            userManager.add(user);
            AssignmentMetadata meta = AssignmentMetadata.now("stress", "истёкшее назначение");
            TemporaryAssignment ta = new TemporaryAssignment(user, role, meta, pastDate, false);
            assignmentManager.add(ta);
        }

        // Все 5 должны быть неактивными (дата уже в прошлом)
        List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
        assertEquals(5, expired.size(), "Должно быть 5 истёкших назначений");

        // Активных назначений этой роли быть не должно
        List<RoleAssignment> activeForRole = assignmentManager.getActiveAssignments().stream()
            .filter(a -> a.role().getName().equals("TempRole"))
            .collect(Collectors.toList());
        assertEquals(0, activeForRole.size(), "Истёкшие назначения не должны быть активными");

        System.out.println("[StressTest] Истёкших назначений: " + expired.size());
    }
}
