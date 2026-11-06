package com.example.shelter.controller;

import com.example.shelter.model.Animal;
import com.example.shelter.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Animal animal : animalService.getAll()) {
            result.add(toJson(animal));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id) {
        return toJson(animalService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Animal animal) {
        return toJson(animalService.create(animal));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Animal animal) {
        return toJson(animalService.update(id, animal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        animalService.delete(id);
    }

    // переводим объект в json для ответа
    private Map<String, Object> toJson(Animal animal) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", animal.getId());
        map.put("name", animal.getName());
        map.put("species", animal.getSpecies());
        map.put("age", animal.getAge());
        map.put("placementStatus", animal.getPlacementStatus());
        map.put("enclosureId", animal.getEnclosureId());
        return map;
    }
}
