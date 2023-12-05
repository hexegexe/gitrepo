package com.example.ToDo.List.Service;

import com.example.ToDo.List.Dto.TaskRequest;
import com.example.ToDo.List.Model.Task;

import java.util.List;

public interface TaskService {

    List<Task> getAllTask();
    Task getTaskById(TaskRequest taskRequest);
    Task addTask(TaskRequest taskRequest);
    Task updateTask(String id ,TaskRequest taskRequest);

    void removeTask(int id);
}
