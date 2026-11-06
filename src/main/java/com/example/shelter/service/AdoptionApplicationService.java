package com.example.shelter.service;

import com.example.shelter.model.AdoptionApplication;
import com.example.shelter.model.Animal;
import com.example.shelter.model.AnimalHandoverRecord;
import com.example.shelter.model.ApplicationStatus;
import com.example.shelter.model.HandoverStatus;
import com.example.shelter.model.PlacementStatus;
import com.example.shelter.repository.AdoptionApplicationRepository;
import com.example.shelter.repository.AnimalHandoverRecordRepository;
import com.example.shelter.repository.AnimalRepository;
import com.example.shelter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import com.example.shelter.security.CurrentUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdoptionApplicationService {

    @Autowired private CurrentUser current;

    @Autowired
    private AdoptionApplicationRepository applicationRepository;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnimalHandoverRecordRepository recordRepository;

    @PreAuthorize("isAuthenticated()")
    public List<AdoptionApplication> getAll() {
        return current.isAdmin() ? applicationRepository.findAll()
                : applicationRepository.findByApplicantId(current.id());
    }

    @PreAuthorize("hasRole('ADMIN') or @access.ownsApplication(#id)")
    public AdoptionApplication getById(Long id) {
        AdoptionApplication application = applicationRepository.findById(id);
        if (application == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена");
        return application;
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public AdoptionApplication create(AdoptionApplication data) {
        if (!current.isAdmin()) {
            if (data.getApplicantId() != null && !current.isSelf(data.getApplicantId()))
                throw new AccessDeniedException("Нельзя подать заявку от чужого имени");
            data.setApplicantId(current.id());
        }
        if (data.getAnimalId() == null || data.getApplicantId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать animalId и applicantId");
        Animal animal = animalRepository.findById(data.getAnimalId());
        if (animal == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Животное не найдено");
        if (userRepository.findById(data.getApplicantId()) == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заявитель не найден");
        if (animal.getPlacementStatus() != PlacementStatus.IN_SHELTER)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Животное уже зарезервировано или передано");
        for (AdoptionApplication a : applicationRepository.findByAnimalId(animal.getId())) {
            if (a.getApplicantId().equals(data.getApplicantId()) && a.getStatus() == ApplicationStatus.PENDING)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "У пользователя уже есть заявка на это животное");
        }

        AdoptionApplication application = new AdoptionApplication();
        application.setAnimalId(data.getAnimalId());
        application.setApplicantId(data.getApplicantId());
        application.setStatus(ApplicationStatus.PENDING); // решение принимается через PATCH
        return applicationRepository.save(application);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @access.ownsApplication(#id)")
    public AdoptionApplication update(Long id, AdoptionApplication data) {
        AdoptionApplication application = getById(id);
        if ((data.getAnimalId() != null && !data.getAnimalId().equals(application.getAnimalId()))
                || (data.getApplicantId() != null && !data.getApplicantId().equals(application.getApplicantId())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Животное и заявителя в заявке менять нельзя");

        ApplicationStatus newStatus = data.getStatus();
        if (!current.isAdmin() && (application.getStatus() != ApplicationStatus.PENDING
                || newStatus != ApplicationStatus.REJECTED))
            throw new AccessDeniedException("Можно только отозвать свою ожидающую заявку");
        if (newStatus == null || newStatus == application.getStatus()) return application;
        Animal animal = animalRepository.findById(application.getAnimalId());

        if (application.getStatus() == ApplicationStatus.PENDING && newStatus == ApplicationStatus.APPROVED) {
            if (animal.getPlacementStatus() != PlacementStatus.IN_SHELTER)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Животное уже зарезервировано или передано");
            animal.setPlacementStatus(PlacementStatus.RESERVED);
            // остальные заявки на это животное отклоняем
            for (AdoptionApplication other : applicationRepository.findByAnimalId(animal.getId())) {
                if (other.getStatus() == ApplicationStatus.PENDING && !other.getId().equals(id))
                    other.setStatus(ApplicationStatus.REJECTED);
            }
        } else if (application.getStatus() == ApplicationStatus.PENDING && newStatus == ApplicationStatus.REJECTED) {
            // просто отклоняем
        } else if (application.getStatus() == ApplicationStatus.APPROVED && newStatus == ApplicationStatus.REJECTED) {
            if (hasActiveRecord(id))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "По заявке уже есть акт передачи");
            animal.setPlacementStatus(PlacementStatus.IN_SHELTER);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя поменять статус " + application.getStatus() + " на " + newStatus);
        }
        application.setStatus(newStatus);
        return application;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @access.ownsApplication(#id)")
    public void delete(Long id) {
        AdoptionApplication application = getById(id);
        if (!current.isAdmin() && application.getStatus() != ApplicationStatus.PENDING)
            throw new AccessDeniedException("Можно удалить только свою ожидающую заявку");
        boolean confirmed = false;
        for (AnimalHandoverRecord r : recordRepository.findByApplicationId(id)) {
            if (r.getStatus() == HandoverStatus.CONFIRMED) confirmed = true;
            recordRepository.deleteById(r.getId());
        }
        // если животное было только зарезервировано по этой заявке - возвращаем его в приют
        Animal animal = animalRepository.findById(application.getAnimalId());
        if (application.getStatus() == ApplicationStatus.APPROVED && !confirmed && animal != null)
            animal.setPlacementStatus(PlacementStatus.IN_SHELTER);
        applicationRepository.deleteById(id);
    }

    private boolean hasActiveRecord(Long applicationId) {
        for (AnimalHandoverRecord r : recordRepository.findByApplicationId(applicationId)) {
            if (r.getStatus() != HandoverStatus.CANCELLED) return true;
        }
        return false;
    }
}
