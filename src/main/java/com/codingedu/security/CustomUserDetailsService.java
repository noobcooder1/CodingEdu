package com.codingedu.security;

import com.codingedu.entity.User;
import com.codingedu.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 아이디로 DB에서 유저 정보를 가져옵니다
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 아이디입니다: " + username));
        
        // SecurityContext에 넣기 위해 CustomUserDetails 로 감싸서 반환
        return new CustomUserDetails(user);
    }
}
