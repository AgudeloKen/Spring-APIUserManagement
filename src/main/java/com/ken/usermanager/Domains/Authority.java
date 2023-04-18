package com.ken.usermanager.Entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Nullable
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "authority", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

}
