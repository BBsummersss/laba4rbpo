package com.example.shelter.service;

import com.example.shelter.model.AdoptionApplication;
import com.example.shelter.model.Animal;
import com.example.shelter.model.Enclosure;
import com.example.shelter.model.PlacementStatus;
import com.example.shelter.repository.AdoptionApplicationRepository;
import com.example.shelter.repository.AnimalRepository;
import com.example.shelter.repository.EnclosureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private EnclosureRepository enclosureRepository;
    @Autowired
    private AdoptionApplicationRepository applicationRepository;
    @Autowired
    private AdoptionApplicationService applicationService;

    @PreAuthorize("isAuthenticated()")
    public List<Animal> getAll() {
        return animalRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    public Animal getById(Long id) {
        Animal animal = animalRepository.findById(id);
        if (animal == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Животное не найдено");
        return animal;
    }

    @Transactional
    public Animal create(Animal data) {
        if (data.getName() == null || data.getName().isBlank() || data.getSpecies() == null
                || data.getSpecies().isBlank() || data.getAge() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать name, species и age");
        if (data.getAge() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возраст не может быть отрицательным");
        if (data.getEnclosureId() != null) checkEnclosure(data.getEnclosureId(), data.getSpecies(), null);

        Animal animal = new Animal();
        animal.setName(data.getName());
        animal.setSpecies(data.getSpecies());
        animal.setAge(data.getAge());
        animal.setEnclosureId(data.getEnclosureId());
        animal.setPlacementStatus(PlacementStatus.IN_SHELTER); // новое животное всегда в приюте
        return animalRepository.save(animal);
    }

    @Transactional
    public Animal update(Long id, Animal data) {
        Animal animal = getById(id);
        // статус меняется только через заявки и акты передачи
        if (data.getPlacementStatus() != null && data.getPlacementStatus() != animal.getPlacementStatus())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус меняется через заявки и акты передачи");
        if (data.getName() != null && data.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Кличка не может быть пустой");
        if (data.getSpecies() != null && data.getSpecies().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вид не может быть пустым");
        if (data.getAge() != null && data.getAge() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возраст не может быть отрицательным");

        String species = data.getSpecies() != null ? data.getSpecies() : animal.getSpecies();
        Long enclosureId = data.getEnclosureId() != null ? data.getEnclosureId() : animal.getEnclosureId();
        if (enclosureId != null) {
            if (animal.getPlacementStatus() == PlacementStatus.ADOPTED)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Животное уже передано в семью");
            checkEnclosure(enclosureId, species, id);
        }

        if (data.getName() != null) animal.setName(data.getName());
        if (data.getAge() != null) animal.setAge(data.getAge());
        animal.setSpecies(species);
        animal.setEnclosureId(enclosureId);
        return animal;
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        for (AdoptionApplication a : applicationRepository.findByAnimalId(id)) {
            applicationService.delete(a.getId());
        }
        animalRepository.deleteById(id);
    }

    // вид должен подходить вольеру и в вольере должно быть место
    private void checkEnclosure(Long enclosureId, String species, Long animalId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId);
        if (enclosure == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вольер не найден");
        boolean allowed = false;
        for (String s : enclosure.getAllowedSpecies()) {
            if (s.equalsIgnoreCase(species)) allowed = true;
        }
        if (!allowed)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Этот вид нельзя держать в этом вольере");
        int count = 0;
        for (Animal a : animalRepository.findByEnclosureId(enclosureId)) {
            if (!a.getId().equals(animalId)) count++;
        }
        if (count >= enclosure.getCapacity())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "В вольере нет свободных мест");
    }
}
