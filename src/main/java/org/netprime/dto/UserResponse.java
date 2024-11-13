package org.netprime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netprime.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String username;
    private String name;
    private String email;
    private Set<SimpleGrantedAuthority> roles;
}
