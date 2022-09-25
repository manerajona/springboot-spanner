package com.github.manerajona.springspanner

import com.google.cloud.NoCredentials
import com.google.cloud.spanner.DatabaseId
import com.google.cloud.spanner.InstanceConfigId
import com.google.cloud.spanner.InstanceId
import com.google.cloud.spanner.InstanceInfo
import com.google.cloud.spanner.Spanner
import com.google.cloud.spanner.SpannerOptions
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.SpannerEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.ExecutionException

@SpringBootTest
@Testcontainers
internal class SpringSpannerModelTests {

    @Autowired
    private lateinit var repository: SingerRepo

    @Test
    internal fun testRepository() {
        repository.save(
            Singer("David Bowie")
        )
        repository.save(
            Singer("Amy Winehouse")
        )
        repository.save(
            Singer("Ella Fitzgerald")
        )

        Assertions.assertThat(repository.findAll().size).isEqualTo(3)
    }

    companion object {

        private const val PROJECT_NAME = "testcontainers-project"
        private const val INSTANCE_NAME = "testcontainers-instance"
        private const val DATABASE_NAME = "testcontainers-database"

        @Container
        internal val emulator: SpannerEmulatorContainer = SpannerEmulatorContainer(
            DockerImageName
                .parse("gcr.io/cloud-spanner-emulator/emulator")
                .withTag("latest")
        )

        @JvmStatic
        @DynamicPropertySource
        internal fun properties(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") {
                "jdbc:cloudspanner://${emulator.emulatorGrpcEndpoint}/" +
                        "projects/${PROJECT_NAME}/" +
                        "instances/${INSTANCE_NAME}/" +
                        "databases/${DATABASE_NAME}?" +
                        "autoConfigEmulator=true"
            }

        @JvmStatic
        @BeforeAll
        internal fun beforeAll() {
            // setup emulator
            val spanner = SpannerOptions
                .newBuilder()
                .setEmulatorHost(emulator.emulatorGrpcEndpoint)
                .setCredentials(NoCredentials.getInstance())
                .setProjectId(PROJECT_NAME)
                .build()
                .service

            // create instance
            createInstance(spanner)

            // create database
            val databaseId = createDatabase(spanner)
            spanner.getDatabaseClient(databaseId)
        }

        @Throws(InterruptedException::class, ExecutionException::class)
        private fun createDatabase(spanner: Spanner): DatabaseId {
            val statements = listOf<String>()
            return spanner.databaseAdminClient
                .createDatabase(INSTANCE_NAME, DATABASE_NAME, statements)
                .get()
                .id
        }

        @Throws(InterruptedException::class, ExecutionException::class)
        private fun createInstance(spanner: Spanner): InstanceId {
            val instanceId = InstanceId.of(PROJECT_NAME, INSTANCE_NAME)
            val instanceConfig = InstanceConfigId.of(PROJECT_NAME, "emulator-config")
            spanner.instanceAdminClient
                .createInstance(
                    InstanceInfo
                        .newBuilder(instanceId)
                        .setNodeCount(1)
                        .setDisplayName("Test instance")
                        .setInstanceConfigId(instanceConfig)
                        .build()
                )
                .get()
            return instanceId
        }
    }
}
