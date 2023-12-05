package com.example.ToDo.List.Controller;

import com.example.ToDo.List.Dto.ErrorResponse;
import com.example.ToDo.List.Dto.TaskRequest;
import com.example.ToDo.List.Dto.TaskResponse;
import com.example.ToDo.List.Model.Task;
import com.example.ToDo.List.Model.TaskStatus.TaskStatus;
import com.example.ToDo.List.Repository.TaskRepository;
import com.example.ToDo.List.Service.TaskServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("")
@RequestMapping("/tasks")
public class TaskController {

    private final TaskServiceImpl service;

    @Autowired
    public TaskController(TaskServiceImpl service) {
        this.service = service;
    }


    @GetMapping("/")
    public ResponseEntity<List<Task>> getAllTasks(){
        return ResponseEntity.ok(service.getAllTask());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable String id){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setId(id);
        Task task = service.getTaskById(taskRequest);
        if(task != null){
            return ResponseEntity.ok(task);
        }else {
            ErrorResponse errorResponse = new ErrorResponse("Not found task with id=" + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/")
    public ResponseEntity<TaskResponse> addTask(@RequestBody TaskRequest taskRequest){
        Task createdTask = service.addTask(taskRequest);
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(createdTask.getId());
        taskResponse.setDescription(createdTask.getDescription());
        taskResponse.setTitle(createdTask.getTitle());
        taskResponse.setStatus(createdTask.getTaskStatus());

        return ResponseEntity.ok(taskResponse);
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> removeTask(@PathVariable String id){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setId(id);
        Task task = service.getTaskById(taskRequest);
        if(task != null){
            int taskId = task.getId();
            service.removeTask(taskId);
            return ResponseEntity.ok("Task with id " + taskId + " removed");
        }
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with id=" + id + " not found");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateTask(@RequestBody TaskRequest taskRequest, @PathVariable String id){
        service.updateTask(id,taskRequest);
        return ResponseEntity.ok("Tasks updatable");
    }
}
