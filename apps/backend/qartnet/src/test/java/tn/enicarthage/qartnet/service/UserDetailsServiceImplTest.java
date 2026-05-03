package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.impl.UserDetailsServiceImpl;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_existingActiveUser_returnsUserDetailsWithRoles() {
        UUID publicId = UUID.randomUUID();
        User user = new User();
        user.setPublicId(publicId);
        user.setPassword("hashed");
        user.setRoles(new HashSet<>(Set.of(Role.STUDENT, Role.ADMIN)));
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByPublicId(publicId)).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername(publicId.toString());

        assertThat(details.getUsername()).isEqualTo(publicId.toString());
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_STUDENT", "ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_pendingUser_isDisabled() {
        UUID publicId = UUID.randomUUID();
        User user = new User();
        user.setPublicId(publicId);
        user.setPassword("hashed");
        user.setRoles(new HashSet<>(Set.of(Role.STUDENT)));
        user.setAccountStatus(AccountStatus.PENDING);

        when(userRepository.findByPublicId(publicId)).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername(publicId.toString());

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_suspendedUser_isLocked() {
        UUID publicId = UUID.randomUUID();
        User user = new User();
        user.setPublicId(publicId);
        user.setPassword("hashed");
        user.setRoles(new HashSet<>(Set.of(Role.STUDENT)));
        user.setAccountStatus(AccountStatus.SUSPENDED);

        when(userRepository.findByPublicId(publicId)).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername(publicId.toString());

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    void loadUserByUsername_unknownUser_throwsUsernameNotFoundException() {
        UUID publicId = UUID.randomUUID();
        when(userRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(publicId.toString()))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
