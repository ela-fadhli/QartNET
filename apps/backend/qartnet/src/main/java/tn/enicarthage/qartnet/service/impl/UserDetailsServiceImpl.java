package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String publicId) throws UsernameNotFoundException {
        User user = userRepository.findByPublicId(UUID.fromString(publicId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String[] roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPublicId().toString())
                .password(user.getPassword())
                .roles(roleNames)
                .disabled(user.getAccountStatus() == AccountStatus.PENDING
                        || user.getAccountStatus() == AccountStatus.DISABLED)
                .accountLocked(user.getAccountStatus() == AccountStatus.SUSPENDED)
                .build();
    }
}
