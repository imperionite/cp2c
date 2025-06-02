package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Interface for user data access operations.
 */
public interface UserDao {
    Optional<User> findByUsername(String username);

    void saveUser(User user); // Can be used for create and update

    void deleteUser(String userId); // for deleting user

    List<User> getAllUsers();
}