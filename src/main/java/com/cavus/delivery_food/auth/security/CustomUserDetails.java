package com.cavus.delivery_food.auth.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
   }
}
