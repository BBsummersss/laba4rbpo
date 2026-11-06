package com.example.shelter.repository;

import com.example.shelter.model.User;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserRepository {
    private final UserJpaRepository dao;
    public UserRepository(UserJpaRepository dao) { this.dao = dao; }
    public List<User> findAll() { return dao.findAll(); }
    public User findById(Long id) { return dao.findById(id).orElse(null); }
    public User findByUsername(String username) { return dao.findByUsername(username).orElse(null); }
    public User findByEmail(String email) { return dao.findByEmail(email).orElse(null); }
    public User save(User user) { return dao.save(user); }
    public void deleteById(Long id) { dao.deleteById(id); }
}
