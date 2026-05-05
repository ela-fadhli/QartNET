package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.impl.UserDetailsServiceImpl;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        UUID publicId = UUID.randomUUID();
        User user = new User();
        user.setPublicId(publicId);
        user.setPassword("hashed");
        user.setRole(Role.USER);

        when(userRepository.findByPublicId(publicId)).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername(publicId.toString());

        assertThat(details.getUsername()).isEqualTo(publicId.toString());
        assertThat(details.getPassword()).isEqualTo("hashed");
    }

    @Test
    void loadUserByUsername_unknownUser_throwsUsernameNotFoundException() {
        UUID publicId = UUID.randomUUID();
        when(userRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(publicId.toString()))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
