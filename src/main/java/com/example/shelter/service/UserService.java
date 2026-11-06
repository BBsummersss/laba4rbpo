package com.example.shelter.service;

import com.example.shelter.model.User;
import com.example.shelter.repository.*;
import com.example.shelter.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class UserService implements UserDetailsService {
    @Autowired private UserRepository userRepository;
    @Autowired private AdoptionApplicationRepository applicationRepository;
    @Autowired private AdoptionApplicationService applicationService;
    @Autowired private PasswordEncoder encoder;
    @Autowired private CurrentUser current;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username.toLowerCase(Locale.ROOT));
        if (user == null || user.getPassword() == null || user.getRole() == null)
            throw new UsernameNotFoundException("Неверный логин или пароль");
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword()).roles(user.getRole())
                .disabled(!Boolean.TRUE.equals(user.getEnabled())).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAll() { return userRepository.findAll(); }

    @PreAuthorize("hasRole('ADMIN') or @access.isSelf(#id)")
    public User getById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        return user;
    }

    @Transactional
    public User register(User data) { return saveNew(data, "USER", true); }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User create(User data) {
        return saveNew(data, data.getRole() == null ? "USER" : data.getRole(),
                data.getEnabled() == null || data.getEnabled());
    }

    private User saveNew(User data, String role, boolean enabled) {
        if (data.getName() == null || data.getName().isBlank()) bad("Не указано имя");
        if (data.getUsername() == null || !data.getUsername().matches("[A-Za-z0-9._-]{3,40}"))
            bad("Логин: 3–40 букв латиницы, цифр, точек, дефисов или подчёркиваний");
        checkPassword(data.getPassword());
        checkRole(role);
        String username = data.getUsername().toLowerCase(Locale.ROOT);
        String email = data.getEmail() == null ? null : data.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByUsername(username) != null
                || (email != null && userRepository.findByEmail(email) != null))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Логин или email уже занят");
        User user = new User();
        user.setName(data.getName()); user.setEmail(email); user.setUsername(username);
        user.setPassword(encoder.encode(data.getPassword())); user.setRole(role); user.setEnabled(enabled);
        return userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @access.isSelf(#id)")
    public User update(Long id, User data) {
        User user = getById(id);
        if (!current.isAdmin() && (data.getRole() != null || data.getEnabled() != null))
            throw new AccessDeniedException("Нельзя менять свои права и состояние");
        if (data.getName() != null) {
            if (data.getName().isBlank()) bad("Имя не может быть пустым");
            user.setName(data.getName());
        }
        if (data.getEmail() != null) {
            String email = data.getEmail().trim().toLowerCase(Locale.ROOT);
            User other = userRepository.findByEmail(email);
            if (other != null && !other.getId().equals(id))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже занят");
            user.setEmail(email);
        }
        if (data.getPassword() != null) {
            checkPassword(data.getPassword()); user.setPassword(encoder.encode(data.getPassword()));
        }
        if (data.getRole() != null) { checkRole(data.getRole()); user.setRole(data.getRole()); }
        if (data.getEnabled() != null) user.setEnabled(data.getEnabled());
        return user;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        getById(id);
        for (var application : applicationRepository.findByApplicantId(id))
            applicationService.delete(application.getId());
        userRepository.deleteById(id);
    }

    private void checkPassword(String password) {
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*") || !password.matches(".*[^A-Za-z0-9].*")
                || password.chars().anyMatch(Character::isWhitespace))
            bad("Пароль: от 8 символов, буквы разного регистра, цифра и спецсимвол, без пробелов; не более 72 байт");
    }
    private void checkRole(String role) {
        if (!role.equals("USER") && !role.equals("ADMIN")) bad("Неизвестная роль");
    }
    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createAdmin() {
        if (userRepository.findByUsername("admin") != null) return;
        User admin = new User();
        admin.setName("Администратор"); admin.setEmail("admin@local.test");
        admin.setUsername("admin"); admin.setPassword(encoder.encode("Admin12345!"));
        admin.setRole("ADMIN"); admin.setEnabled(true);
        userRepository.save(admin);
    }
}
