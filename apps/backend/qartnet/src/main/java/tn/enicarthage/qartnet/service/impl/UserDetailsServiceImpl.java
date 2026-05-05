package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            UUID publicId = UUID.fromString(username);
            user = userRepository.findByPublicId(publicId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (IllegalArgumentException e) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPublicId().toString())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
