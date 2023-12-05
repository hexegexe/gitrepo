package com.example.ToDo.List.Dto;

import com.example.ToDo.List.Model.TaskStatus.TaskStatus;
import lombok.Data;

@Data
public class TaskResponse {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
}
