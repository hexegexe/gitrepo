package com.example.ToDo.List.Model;

import com.example.ToDo.List.Model.TaskStatus.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private  String description;
    private TaskStatus taskStatus;

    @ManyToOne
    private User user;
}
