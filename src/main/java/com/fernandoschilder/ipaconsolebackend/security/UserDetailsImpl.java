package com.fernandoschilder.ipaconsolebackend.security;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fernandoschilder.ipaconsolebackend.model.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private boolean enabled;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getUser_roles().stream().map(role -> new SimpleGrantedAuthority(role.getName())) // Assuming Role has a 'getName()' method
                .collect(Collectors.toList());

        return new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword(), user.isEnabled(), authorities);
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}