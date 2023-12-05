package com.example.ToDo.List.Repository;

import com.example.ToDo.List.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    User findByEmail(String email);
    User findByFirstName(String firstName);
    User findByFirstNameAndLastName(String firstName, String lastName);
}
