package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.User;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByUsername(String username);

    Optional<Profile> findByUsername(String username);

    Optional<Profile> findByUser(User user);
}
