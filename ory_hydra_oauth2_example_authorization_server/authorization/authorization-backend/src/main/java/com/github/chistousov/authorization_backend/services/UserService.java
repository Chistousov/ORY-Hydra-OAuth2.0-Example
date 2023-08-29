package com.github.chistousov.authorization_backend.services;

import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.chistousov.authorization_backend.dao.entities.User;
import com.github.chistousov.authorization_backend.dao.repositories.UserRepository;
import com.github.chistousov.authorization_backend.exceptions.IncorrectPasswordException;
import com.github.chistousov.authorization_backend.exceptions.LoginDoesNotExistException;
import com.github.chistousov.authorization_backend.jacoco_ignore.ExcludeFromJacocoGeneratedReport;
import com.github.chistousov.authorization_backend.models.PostRegistrationModel;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * <p>
 * Service for working with the user (creation and verification)
 * </p>
 * 
 * @author Nikita Chistousov (chistousov.nik@yandex.ru)
 * @since 11
 */
@Service
public class UserService {

    private TransactionTemplate transactionTemplate;
    private Scheduler jdbcScheduler;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoderRegister) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoderRegister;
    }

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Autowired
    @Qualifier("jdbcScheduler")
    public void setJdbcScheduler(Scheduler jdbcScheduler) {
        this.jdbcScheduler = jdbcScheduler;
    }

    /**
     * 
     * <p>
     * Create a user
     * </p>
     * 
     * 
     * @param postRegistrationModel - registration data
     * 
     * @return user id
     * 
     * @author Nikita Chistousov (chistousov.nik@yandex.ru)
     * @since 11
     */
    public Mono<Long> createUser(PostRegistrationModel postRegistrationModel) {

        return Mono.fromCallable(
                () -> {

                    String hashPassword = passwordEncoder.encode(postRegistrationModel.getPassword());

                    return transaction(() -> userRepository.createUser(
                            postRegistrationModel.getLogin(),
                            hashPassword,
                            postRegistrationModel.getOrgName()));
                })
                .subscribeOn(jdbcScheduler);

    }

    /**
     * 
     * <p>
     * Get user by login and check user
     * </p>
     * 
     * 
     * @param login    - login
     * 
     * @param password - password
     * 
     * @return user data
     * 
     * @author Nikita Chistousov (chistousov.nik@yandex.ru)
     * @since 11
     */
    public Mono<User> getUser(String login, String password) {

        return Mono.fromCallable(() -> {

            User user = transaction(() -> {

                var storedProcedure = em.createNamedStoredProcedureQuery("User.getUserByLogin");
                storedProcedure.setParameter(2, login);
                storedProcedure.execute();

                List<User> users = (List<User>) storedProcedure.getResultList();

                if (!users.isEmpty()) {
                    return users.get(0);
                } else {
                    throw new LoginDoesNotExistException("login does not exist");
                }

            });

            if (passwordEncoder.matches(password, user.getPassword())) {
                user.setPassword("");

                return user;
            } else {
                throw new IncorrectPasswordException("Password is incorrect! ");
            }
        })
        .subscribeOn(jdbcScheduler);
    }

    /**
     * <p>
     * Дополнительная обертка для ручной транзакции для игнорирования в JaCoCo
     * </p>
     * 
     * @param manId - пользователь в системе
     * 
     * @return информация о пользователе
     * 
     * @author Nikita Chistousov (chistousov.nik@yandex.ru)
     * @since 11
     */
    @ExcludeFromJacocoGeneratedReport
    private <T> T transaction(Supplier<T> s) {
        return transactionTemplate.execute(status -> s.get());
    }
}
