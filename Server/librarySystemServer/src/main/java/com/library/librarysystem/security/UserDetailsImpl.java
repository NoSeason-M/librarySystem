package com.library.librarysystem.security;

import com.library.librarysystem.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String realName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserDetailsImpl(SysUser user, List<String> roleCodes) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.realName = user.getRealName();
        this.authorities = roleCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        this.enabled = user.getStatus() == 1;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
