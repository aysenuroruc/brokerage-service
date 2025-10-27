package model.entity;

import jakarta.persistence.*;
import lombok.Data;
import model.Role;

@Data
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Customer() {}
}
