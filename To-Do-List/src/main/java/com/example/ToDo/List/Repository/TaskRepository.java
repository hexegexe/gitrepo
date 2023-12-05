package com.example.ToDo.List.Repository;

import com.example.ToDo.List.Model.Task;
import com.example.ToDo.List.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {
    List<Task> findAllByUser(User user);
    Task findById(int id);

    void deleteById(Task task);

    Task findByTitle(String title);

}
