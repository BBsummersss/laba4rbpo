package com.example.shelter.controller;

import com.example.shelter.model.AdoptionApplication;
import com.example.shelter.service.AdoptionApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adoption-applications")
public class AdoptionApplicationController {

    @Autowired
    private AdoptionApplicationService adoptionApplicationService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AdoptionApplication application : adoptionApplicationService.getAll()) {
            result.add(toJson(application));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id) {
        return toJson(adoptionApplicationService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody AdoptionApplication application) {
        return toJson(adoptionApplicationService.create(application));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody AdoptionApplication application) {
        return toJson(adoptionApplicationService.update(id, application));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adoptionApplicationService.delete(id);
    }

    // переводим объект в json для ответа
    private Map<String, Object> toJson(AdoptionApplication application) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", application.getId());
        map.put("animalId", application.getAnimalId());
        map.put("applicantId", application.getApplicantId());
        map.put("status", application.getStatus());
        return map;
    }
}
