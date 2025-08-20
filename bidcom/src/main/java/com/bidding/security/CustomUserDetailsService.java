// CustomUserDetailsService.java
package com.bidding.security;

import com.bidding.model.Admin;
import com.bidding.model.User;
import com.bidding.repository.AdminRepository;
import com.bidding.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;
    private final AdminRepository adminRepo;

    public CustomUserDetailsService(UserRepository userRepo, AdminRepository adminRepo) {
        this.userRepo = userRepo;
        this.adminRepo = adminRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isPresent()) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.get().getUsername())
                    .password(user.get().getPassword())
                    .roles("USER")
                    .build();
        }

        Optional<Admin> admin = adminRepo.findByUsername(username);
        if (admin.isPresent()) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(admin.get().getUsername())
                    .password(admin.get().getPassword())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }
}
