package com.ecommerce.project.security.services;

import com.ecommerce.project.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UserDetails} interface used by Spring Security for authentication and authorization.
 * <p>
 * This class wraps user information retrieved from the application's {@link User} entity and transforms roles
 * into Spring Security {@link GrantedAuthority} objects.
 * </p>
 *
 * @author
 */
@NoArgsConstructor
@Data
public class Userdetailsimpl implements UserDetails {

    private static final long serialVersionId = 1L;

    /**
     * The user's ID.
     */
    private Long id;

    /**
     * The user's username used for login.
     */
    private String username;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The user's password (hidden from JSON serialization).
     */
    @JsonIgnore
    private String password;

    /**
     * A collection of authorities granted to the user (e.g., roles).
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a {@code UserDetailsServiceImpl} with all user-related attributes.
     *
     * @param id          The user's ID.
     * @param password    The user's password.
     * @param email       The user's email address.
     * @param username    The user's username.
     * @param authorities The granted authorities (roles).
     */
    public Userdetailsimpl(Long id, String password, String email, String username,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.username = username;
        this.authorities = authorities;
    }

    /**
     * Builds a {@code UserDetailsServiceImpl} object from a {@link User} entity.
     *
     * @param user The user entity.
     * @return A {@code UserDetailsServiceImpl} instance containing security-specific user details.
     */
    public static Userdetailsimpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        return new Userdetailsimpl(
                user.getUserId(),
                user.getPassword(),
                user.getEmail(),
                user.getUserName(),
                authorities
        );
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return A collection of granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return The user's password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return The user's username.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired. Always returns {@code true}.
     *
     * @return {@code true} if the account is non-expired; {@code false} otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is locked. Always returns {@code true}.
     *
     * @return {@code true} if the account is non-locked; {@code false} otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired. Always returns {@code true}.
     *
     * @return {@code true} if the credentials are non-expired; {@code false} otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled. Always returns {@code true}.
     *
     * @return {@code true} if the user is enabled; {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }


     @Override
    public boolean equals(Object o){
        if(this==o){
            return true;
        }
        if(o==null||getClass()!=o.getClass())
            return false;
        Userdetailsimpl user=(Userdetailsimpl) o;
        return Objects.equals(id,user.id);
     }
}
