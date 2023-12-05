package com.example.ToDo.List.Dto;

import com.example.ToDo.List.Model.TaskStatus.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

    private String id;
    private String title;
    private String description;
    private TaskStatus status;
}
