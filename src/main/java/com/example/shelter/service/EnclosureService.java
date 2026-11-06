package com.example.shelter.service;

import com.example.shelter.model.Animal;
import com.example.shelter.model.Enclosure;
import com.example.shelter.repository.AnimalRepository;
import com.example.shelter.repository.EnclosureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class EnclosureService {

    @Autowired
    private EnclosureRepository enclosureRepository;
    @Autowired
    private AnimalRepository animalRepository;

    @PreAuthorize("isAuthenticated()")
    public List<Enclosure> getAll() {
        return enclosureRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    public Enclosure getById(Long id) {
        Enclosure enclosure = enclosureRepository.findById(id);
        if (enclosure == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вольер не найден");
        return enclosure;
    }

    @Transactional
    public Enclosure create(Enclosure data) {
        if (data.getCapacity() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать allowedSpecies и capacity");
        checkSpecies(data.getAllowedSpecies());
        if (data.getCapacity() < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вместимость должна быть больше 0");

        Enclosure enclosure = new Enclosure();
        enclosure.setAllowedSpecies(new ArrayList<>(data.getAllowedSpecies()));
        enclosure.setCapacity(data.getCapacity());
        return enclosureRepository.save(enclosure);
    }

    @Transactional
    public Enclosure update(Long id, Enclosure data) {
        Enclosure enclosure = getById(id);
        List<Animal> animals = animalRepository.findByEnclosureId(id);

        if (data.getCapacity() != null) {
            if (data.getCapacity() < 1)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вместимость должна быть больше 0");
            if (data.getCapacity() < animals.size())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "В вольере уже больше животных, чем новая вместимость");
        }
        if (data.getAllowedSpecies() != null) {
            checkSpecies(data.getAllowedSpecies());
            // животные, которые уже живут в вольере, должны остаться допустимыми
            for (Animal a : animals) {
                boolean ok = false;
                for (String s : data.getAllowedSpecies()) {
                    if (s.equalsIgnoreCase(a.getSpecies())) ok = true;
                }
                if (!ok)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "В вольере живёт животное вида " + a.getSpecies());
            }
        }

        if (data.getCapacity() != null) enclosure.setCapacity(data.getCapacity());
        if (data.getAllowedSpecies() != null) enclosure.setAllowedSpecies(new ArrayList<>(data.getAllowedSpecies()));
        return enclosure;
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        // животные остаются в приюте, просто без вольера
        for (Animal a : animalRepository.findByEnclosureId(id)) {
            a.setEnclosureId(null);
        }
        enclosureRepository.deleteById(id);
    }

    private void checkSpecies(List<String> species) {
        if (species == null || species.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать хотя бы один вид");
        for (String s : species) {
            if (s == null || s.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вид не может быть пустым");
        }
    }
}
