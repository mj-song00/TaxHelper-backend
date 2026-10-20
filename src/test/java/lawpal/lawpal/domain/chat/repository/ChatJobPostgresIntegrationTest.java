package lawpal.lawpal.domain.chat.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import lawpal.lawpal.domain.chat.dto.request.CompletedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.request.PreparedChatJobRequest;
import lawpal.lawpal.domain.chat.entity.ChatJob;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;
import lawpal.lawpal.domain.chat.service.ChatJobService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static lawpal.lawpal.domain.chat.enums.ChatJobStatus.*;

/** Opt-in only: disposable PostgreSQL, no application configuration or development DB fallback. */
@EnabledIfEnvironmentVariable(named = "TAXHELPER_CAS_TEST_URL",
        matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/taxhelper_cas_test")
class ChatJobPostgresIntegrationTest {
    static AnnotationConfigApplicationContext context;
    static ChatJobRepository repository;
    static ChatJobService service;
    static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    static final LocalDateTime NOW = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = ChatJobRepository.class)
    static class DatabaseOnlyConfiguration {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource(System.getenv("TAXHELPER_CAS_TEST_URL"),
                    "cas_test", System.getenv("TAXHELPER_CAS_TEST_PASSWORD"));
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("lawpal.lawpal.domain.chat.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean ChatJobService chatJobService(ChatJobRepository repository) {
            return new ChatJobService(repository, mapper, "http://unused.test");
        }
    }

    @BeforeAll static void openIsolatedDatabase() throws Exception {
        // Connection parameters are explicit opt-in values, never application.yml or .env.
        context = new AnnotationConfigApplicationContext(DatabaseOnlyConfiguration.class);
        repository = context.getBean(ChatJobRepository.class);
        service = context.getBean(ChatJobService.class);
        try (var connection = context.getBean(DataSource.class).getConnection()) {
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
            assertEquals("taxhelper_cas_test", connection.getCatalog());
        }
    }
    @AfterAll static void closeIsolatedDatabase() {
        if (context != null) context.close();
    }
    @AfterEach void removeTestRows() { repository.deleteAllInBatch(); }

    private UUID seed(ChatJobStatus status) {
        ChatJob job = ChatJob.builder().jobId(UUID.randomUUID()).question("question").topK(5)
                .status(status).context("original context").sources("[]")
                .answer(status == COMPLETED ? "original answer" : null)
                .errorMessage(status == FAILED ? "original error" : null)
                .processingStartedAt(status == PROCESSING || status == COMPLETED || status == FAILED ? NOW : null)
                .completedAt(status == COMPLETED || status == FAILED ? NOW : null).build();
        return repository.saveAndFlush(job).getJobId(); // repository transaction commits before callbacks/threads
    }

    @Test void conditionalUpdatesRespectStateMatrix() {
        List<BiFunction<UUID, LocalDateTime, Integer>> updates = List.of(
                (id, now) -> repository.prepareIfPreparing(id, "new context", "[1]", now),
                repository::waitingIfPreparing, repository::processingIfWaiting,
                (id, now) -> repository.completeIfProcessing(id, "new answer", now),
                (id, now) -> repository.failIfStatusIn(id, Set.of(PREPARING, WAITING, PROCESSING), "new error", now));
        List<Set<ChatJobStatus>> allowed = List.of(Set.of(PREPARING), Set.of(PREPARING),
                Set.of(WAITING), Set.of(PROCESSING), Set.of(PREPARING, WAITING, PROCESSING));
        List<ChatJobStatus> targets = List.of(PREPARING, WAITING, PROCESSING, COMPLETED, FAILED);
        for (ChatJobStatus start : ChatJobStatus.values()) {
            for (int i = 0; i < updates.size(); i++) {
                UUID id = seed(start);
                var before = mapper.valueToTree(repository.findByJobId(id).orElseThrow());
                int changed = updates.get(i).apply(id, NOW.plusSeconds(1));
                ChatJob after = repository.findByJobId(id).orElseThrow();
                assertEquals(allowed.get(i).contains(start) ? 1 : 0, changed, start + "/" + i);
                if (changed == 0) assertEquals(before, mapper.valueToTree(after));
                else {
                    assertEquals(targets.get(i), after.getStatus());
                    assertEquals(NOW.plusSeconds(1), after.getUpdatedAt());
                    if (i == 0) { assertEquals("new context", after.getContext()); assertEquals("[1]", after.getSources()); }
                    if (i == 2) assertEquals(NOW.plusSeconds(1), after.getProcessingStartedAt());
                    if (i == 3) { assertEquals("new answer", after.getAnswer()); assertNull(after.getErrorMessage()); }
                    if (i == 4) assertEquals("new error", after.getErrorMessage());
                    if (i >= 3) assertEquals(NOW.plusSeconds(1), after.getCompletedAt());
                }
            }
            UUID id = seed(start);
            var before = mapper.valueToTree(repository.findByJobId(id).orElseThrow());
            assertEquals(start == PREPARING ? 1 : 0,
                    repository.failIfStatusIn(id, Set.of(PREPARING), "registration error", NOW.plusSeconds(1)));
            if (start != PREPARING) assertEquals(before, mapper.valueToTree(repository.findByJobId(id).orElseThrow()));
        }
    }

    @Test void completedThenFailedAndDuplicatesPreserveEveryField() {
        UUID completed = seed(PROCESSING);
        service.markCompleted(completed, new CompletedChatJobRequest("first answer"));
        UUID failed = seed(PROCESSING);
        service.markFailed(failed, "first error");
        for (UUID id : List.of(completed, failed)) {
            var before = mapper.valueToTree(repository.findByJobId(id).orElseThrow());
            for (int repeat = 0; repeat < 2; repeat++) {
                service.markFailed(id, "late error");
                service.markCompleted(id, new CompletedChatJobRequest("late answer"));
                service.markWaiting(id);
                service.savePrepared(id, new PreparedChatJobRequest("late context", "[2]"));
                // Even an erroneous caller supplying terminal states cannot bypass the query guard.
                assertEquals(0, repository.failIfStatusIn(id, EnumSet.allOf(ChatJobStatus.class), "bad", NOW.plusDays(1)));
                assertEquals(before, mapper.valueToTree(repository.findByJobId(id).orElseThrow()));
            }
        }
    }

    @Test void concurrentClaimsHaveSingleWinner() throws Exception {
        UUID id = seed(WAITING);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        Callable<Integer> claim = () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("start gate timeout");
            return repository.processingIfWaiting(id, NOW.plusSeconds(1));
        };
        try {
            Future<Integer> first = pool.submit(claim), second = pool.submit(claim);
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            assertEquals(1, first.get(10, TimeUnit.SECONDS) + second.get(10, TimeUnit.SECONDS));
            assertEquals(PROCESSING, repository.findByJobId(id).orElseThrow().getStatus());
        } finally { start.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }

    @Test void completionAndFailurePreserveWinningTerminalState() throws Exception {
        UUID id = seed(PROCESSING);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            Future<Integer> completed = pool.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("start gate timeout");
                return repository.completeIfProcessing(id, "winning answer", NOW.plusSeconds(1));
            });
            Future<Integer> failed = pool.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("start gate timeout");
                return repository.failIfStatusIn(id, Set.of(PROCESSING), "winning error", NOW.plusSeconds(2));
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            int success = completed.get(10, TimeUnit.SECONDS), failure = failed.get(10, TimeUnit.SECONDS);
            assertEquals(1, success + failure);
            ChatJob winner = repository.findByJobId(id).orElseThrow();
            assertEquals(success == 1 ? COMPLETED : FAILED, winner.getStatus());
            assertEquals(success == 1 ? "winning answer" : null, winner.getAnswer());
            assertEquals(success == 1 ? null : "winning error", winner.getErrorMessage());
            assertEquals(NOW.plusSeconds(success == 1 ? 1 : 2), winner.getCompletedAt());
            var snapshot = mapper.valueToTree(winner);
            service.markFailed(id, "late error");
            service.markCompleted(id, new CompletedChatJobRequest("late answer"));
            assertEquals(snapshot, mapper.valueToTree(repository.findByJobId(id).orElseThrow()));
        } finally { start.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }
}
