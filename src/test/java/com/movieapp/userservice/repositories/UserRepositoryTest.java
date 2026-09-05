package com.movieapp.userservice.repositories;

import com.movieapp.userservice.configurations.TestContainersConfig;
import com.movieapp.userservice.models.AuthProvider;
import com.movieapp.userservice.models.Role;
import com.movieapp.userservice.models.Users;
import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.TimeZone;

@DataJpaTest
@Import(TestContainersConfig.class)
public class UserRepositoryTest {


    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    @Autowired
    UserRepository repo;

    @Autowired
    private EntityManager entityManager;



    @Test
    void shouldFindUserByEmail() {
        Users user = new Users();
        user.setEmail("dhanushch3@gmail.com");
        user.setFirstName("Dhanush");
        user.setLastName("Ch");
        user.setMobile("6301580665");
        user.setUsername("Dhanush58066");
        user.setPassword("Encoded password");
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(List.of(Role.USER,Role.ADMIN));
        repo.save(user);
        entityManager.flush();
        entityManager.clear();
        Users dbUser = repo.findByEmail(user.getEmail()).get();
        entityManager.detach(dbUser);
        assertThat(dbUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(dbUser.getRoles())
                .containsExactly(Role.USER,Role.ADMIN);

        assertThatThrownBy(() -> dbUser.getSessions().getFirst())
                .isInstanceOf(LazyInitializationException.class);

    }
}
