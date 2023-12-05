package com.example.ToDo.List.Service;

import com.example.ToDo.List.Dto.TaskRequest;
import com.example.ToDo.List.Exception.NotFoundTaskException;
import com.example.ToDo.List.Model.Task;
import com.example.ToDo.List.Model.TaskStatus.TaskStatus;
import com.example.ToDo.List.Repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService{


    private final TaskRepository taskRepository;
    @Override
    public List<Task> getAllTask() {
        return taskRepository.findAll();
    }

    @Override
    public Task getTaskById(TaskRequest taskRequest) {
        Task task = taskRepository.findById(Integer.parseInt(taskRequest.getId()));
        if(task == null){
//            throw new NotFoundTaskException("Not found task with id " + task.getId());
            return task;
        }
        return task;
    }

    @Override
    public Task addTask(TaskRequest taskRequest) {
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setTaskStatus(TaskStatus.CREATED);
        taskRepository.save(task);
        return task;
    }

    @Override
    public Task updateTask(String id ,TaskRequest taskRequest) {
        Task task = taskRepository.findById(Integer.parseInt(id));
        if(task !=null) {
            task.setTitle(taskRequest.getTitle() != null ? taskRequest.getTitle() : task.getTitle());
            task.setDescription(taskRequest.getDescription() != null ? taskRequest.getDescription() : task.getDescription());

            if (taskRequest.getStatus().equals(TaskStatus.CREATED) ||
                    taskRequest.getStatus().equals(TaskStatus.AtWORK) ||
                    taskRequest.getStatus().equals(TaskStatus.COMPLETED)) {
                task.setTaskStatus(taskRequest.getStatus());
            }
            taskRepository.save(task);
        }else{
            throw new NotFoundTaskException("Not found task in DB");
        }
        return task;
    }

    @Override
    public void removeTask(int id) {
        taskRepository.deleteById(id);
    }
}
