package com.example.shelter.security;

import com.example.shelter.model.User;
import com.example.shelter.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("access")
public class CurrentUser {
    private final UserRepository users;
    private final AdoptionApplicationRepository applications;
    private final AnimalHandoverRecordRepository records;
    public CurrentUser(UserRepository users, AdoptionApplicationRepository applications,
                       AnimalHandoverRecordRepository records) {
        this.users = users; this.applications = applications; this.records = records;
    }
    public User user() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByUsername(username);
    }
    public Long id() { return user().getId(); }
    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    public boolean isSelf(Long id) { return id().equals(id); }
    public boolean ownsApplication(Long id) {
        var application = applications.findById(id);
        return application != null && isSelf(application.getApplicantId());
    }
    public boolean ownsRecord(Long id) {
        var record = records.findById(id);
        return record != null && ownsApplication(record.getApplicationId());
    }
}
