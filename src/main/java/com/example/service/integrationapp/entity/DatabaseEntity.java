package com.example.service.integrationapp.entity;

import com.example.service.integrationapp.model.EntityModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.dialect.Database;

import java.io.Serializable;
import java.util.UUID;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "entities")
public class DatabaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private Instant date;
    public static DatabaseEntity from(EntityModel model){
        return new DatabaseEntity(model.getId(), model.getName(), model.getDate());
    }
}
