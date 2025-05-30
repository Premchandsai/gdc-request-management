package com.gdc.requests_management.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_role_data")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID user_id;
    private UUID role_id;
}