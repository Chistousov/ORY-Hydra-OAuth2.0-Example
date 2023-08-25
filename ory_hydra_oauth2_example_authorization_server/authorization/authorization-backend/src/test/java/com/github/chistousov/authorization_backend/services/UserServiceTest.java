package com.github.chistousov.authorization_backend.services;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.chistousov.authorization_backend.dao.entities.User;
import com.github.chistousov.authorization_backend.exceptions.IncorrectPasswordException;
import com.github.chistousov.authorization_backend.exceptions.LoginDoesNotExistException;
import com.github.chistousov.authorization_backend.models.PostRegistrationModel;

import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
public class UserServiceTest {

    private static final File pathToResourceDockerComposeFile = Path.of("").toAbsolutePath().getParent().getParent()
            .resolve("docker-compose.yaml").toFile();
    private static final String postgresqlUserDataContainerDockerCompose = "ory-hydra-oauth2-ex-auth-server-userdata-postgresql";
    private static final int postgresqlUserDataPortDockerCompose = 5432;
    private static final String postgresqlUserDataUserDockerCompose = "user_data";
    private static final String postgresqlUserDataPassDockerCompose = "superpass";

    @Container
    public static DockerComposeContainer<?> containersDockerCompose = new DockerComposeContainer<>(
            pathToResourceDockerComposeFile)
            .withEnv("USER_DATA_POSTGRESQL_PASSWORD", postgresqlUserDataPassDockerCompose)
            .withExposedService(postgresqlUserDataContainerDockerCompose, postgresqlUserDataPortDockerCompose,
                    Wait.forSuccessfulCommand(
                            String.format("pg_isready -U %s", postgresqlUserDataUserDockerCompose))
                            .withStartupTimeout(Duration.ofMinutes(1)))
            // .yaml v2 <-> v3
            .withOptions("--compatibility")
            // docker-compose local
            .withLocalCompose(true);

    @DynamicPropertySource
    public static void postgresqlUserDataSettings(DynamicPropertyRegistry registry)
            throws UnsupportedEncodingException {

        registry.add("spring.datasource.driverClassName", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");

        String postgresqlStringConn = String.format(
                "jdbc:postgresql://%s:%d/%s?user=%s&password=%s&escapeSyntaxCallMode=callIfNoReturn",
                containersDockerCompose.getServiceHost(postgresqlUserDataContainerDockerCompose,
                        postgresqlUserDataPortDockerCompose),
                containersDockerCompose.getServicePort(postgresqlUserDataContainerDockerCompose,
                        postgresqlUserDataPortDockerCompose),
                postgresqlUserDataUserDockerCompose,
                postgresqlUserDataUserDockerCompose,
                postgresqlUserDataPassDockerCompose);

        registry.add("spring.datasource.url", () -> postgresqlStringConn);

        registry.add("application.ory-hydra.admin.baseURI", () -> "");

    }

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Successful user creation and attempt to re-create it")
    void testCreateUser() {
        // given (instead of when)

        final String login1 = "someuser1";
        final String login2 = "someuser2";

        final String pass = "asdasfdasffsadfa";

        final String org1 = "someorg1";
        final String org2 = "someorg2";
        
        
        final PostRegistrationModel user1 = PostRegistrationModel
                .builder()
                .login(login1)
                .password(pass)
                .orgName(org1)
                .build();

        final PostRegistrationModel user2 = PostRegistrationModel
                .builder()
                .login(login1)
                .password(pass)
                .orgName(org2)
                .build();

        final PostRegistrationModel user3 = PostRegistrationModel
                .builder()
                .login(login2)
                .password(pass)
                .orgName(org1)
                .build();

        final PostRegistrationModel user4 = PostRegistrationModel
                .builder()
                .login(login2)
                .password(pass)
                .orgName(org2)
                .build();


        // when
        StepVerifier
                .create(userService.createUser(user1))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(userService.createUser(user2))
                .verifyError();

        StepVerifier
                .create(userService.createUser(user3))
                .verifyError();

        StepVerifier
                .create(userService.createUser(user4))
                .expectNextCount(1)
                .verifyComplete();

        // then (instead of verify)

    }
    
    @Test
    @DisplayName("Successful user creation and subsequent selection")
    void testGetUser() {

        // given (instead of when)

        final String login1 = "someuser1_";
        final String login2 = "someuser2_";

        final String pass1 = "zxcvxbxvvcxvxcvxcv";
        final String pass2 = "jdfglkvjbjbjbj";

        final String org1 = "someorg1_";

        final PostRegistrationModel user = PostRegistrationModel
                .builder()
                .login(login1)
                .password(pass1)
                .orgName(org1)
                .build();

        final User expectedUser = User.builder()
                                        .login(login1)
                                        .password("")
                                        .orgName(org1)
                                        .build();

        StepVerifier
                .create(userService.createUser(user))
                .expectNextCount(1)
                .verifyComplete();

        // when

        StepVerifier
                .create(userService.getUser(user.getLogin(), user.getPassword()))
                .expectNext(expectedUser)
                .verifyComplete();

        
        StepVerifier
                .create(userService.getUser(login2, pass1))
                .verifyErrorMatches(ex -> 
                    ex instanceof LoginDoesNotExistException && ex.getMessage().equals("login does not exist")
                );

        StepVerifier
                .create(userService.getUser(login1, pass2))
                .verifyErrorMatches(ex -> 
                    ex instanceof IncorrectPasswordException && ex.getMessage().equals("Password is incorrect! ")
                );

    }
}
