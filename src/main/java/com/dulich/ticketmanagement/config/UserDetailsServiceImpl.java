package com.dulich.ticketmanagement.config;

import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = user.getRole().name();
        // Ánh xạ STAFF thành NHANVIEN nếu có dữ liệu cũ
        if ("STAFF".equals(roleName)) {
            roleName = "NHANVIEN";
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() != null && user.getStatus(),
                true,
                true,
                true,
                authorities
        );
    }
}
