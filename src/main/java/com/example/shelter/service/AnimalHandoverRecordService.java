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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.shelter.security.CurrentUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AnimalHandoverRecordService {

    @Autowired private CurrentUser current;

    @Autowired
    private AnimalHandoverRecordRepository recordRepository;
    @Autowired
    private AdoptionApplicationRepository applicationRepository;
    @Autowired
    private AnimalRepository animalRepository;

    @PreAuthorize("isAuthenticated()")
    public List<AnimalHandoverRecord> getAll() {
        return current.isAdmin() ? recordRepository.findAll()
                : recordRepository.findAll().stream().filter(r -> current.ownsApplication(r.getApplicationId())).toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @access.ownsRecord(#id)")
    public AnimalHandoverRecord getById(Long id) {
        AnimalHandoverRecord record = recordRepository.findById(id);
        if (record == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Акт передачи не найден");
        return record;
    }

    @Transactional
    public AnimalHandoverRecord create(AnimalHandoverRecord data) {
        if (data.getApplicationId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать applicationId");
        AdoptionApplication application = applicationRepository.findById(data.getApplicationId());
        if (application == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заявка не найдена");
        if (application.getStatus() != ApplicationStatus.APPROVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Акт можно составить только по одобренной заявке");
        if (!recordRepository.findByApplicationId(application.getId()).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "По этой заявке уже есть акт");

        AnimalHandoverRecord record = new AnimalHandoverRecord();
        record.setApplicationId(data.getApplicationId());
        record.setPreparedAt(LocalDateTime.now()); // дату подготовки ставит сервер
        record.setConfirmedAt(null);
        record.setStatus(HandoverStatus.PREPARED);
        return recordRepository.save(record);
    }

    @Transactional
    public AnimalHandoverRecord update(Long id, AnimalHandoverRecord data) {
        AnimalHandoverRecord record = getById(id);
        if (data.getApplicationId() != null && !data.getApplicationId().equals(record.getApplicationId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заявку в акте менять нельзя");
        if (data.getStatus() == null || data.getStatus() == record.getStatus()) return record;
        if (record.getStatus() != HandoverStatus.PREPARED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Акт уже подтверждён или отменён");

        if (data.getStatus() == HandoverStatus.CONFIRMED) {
            record.setConfirmedAt(LocalDateTime.now()); // дату подтверждения ставит сервер
            // животное уходит в семью и больше не занимает вольер
            AdoptionApplication application = applicationRepository.findById(record.getApplicationId());
            Animal animal = animalRepository.findById(application.getAnimalId());
            animal.setPlacementStatus(PlacementStatus.ADOPTED);
            animal.setEnclosureId(null);
        }
        record.setStatus(data.getStatus());
        return record;
    }

    @Transactional
    public void delete(Long id) {
        AnimalHandoverRecord record = getById(id);
        if (record.getStatus() == HandoverStatus.CONFIRMED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Подтверждённый акт удалять нельзя");
        recordRepository.deleteById(id);
    }
}
