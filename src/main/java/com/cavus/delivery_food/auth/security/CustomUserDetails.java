package com.cavus.delivery_food.auth.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cavus.delivery_food.auth.entity.Permission;
import com.cavus.delivery_food.auth.entity.Role;
import com.cavus.delivery_food.auth.entity.User;

// ! Burada UserDetails interface'ini implemente ediyoruz ve bu interface'in methodlarını override ediyoruz.
public class CustomUserDetails implements UserDetails{

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }


   /// Bu sayede aynı anda hem `hasRole("ADMIN")` (rol bazlı) hem `hasAuthority("PRODUCT_DELETE")` (izin bazlı) kontrolleri mümkün olur.
  @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return authorities;
    }
}
