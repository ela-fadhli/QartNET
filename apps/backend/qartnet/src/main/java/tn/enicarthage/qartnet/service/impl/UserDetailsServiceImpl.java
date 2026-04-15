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
    public UserDetails loadUserByUsername(String publicId) throws UsernameNotFoundException {
        User user = userRepository.findByPublicId(UUID.fromString(publicId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPublicId().toString())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
