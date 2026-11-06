package com.example.shelter.controller;

import com.example.shelter.model.AnimalHandoverRecord;
import com.example.shelter.service.AnimalHandoverRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animal-handover-records")
public class AnimalHandoverRecordController {

    @Autowired
    private AnimalHandoverRecordService animalHandoverRecordService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AnimalHandoverRecord record : animalHandoverRecordService.getAll()) {
            result.add(toJson(record));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id) {
        return toJson(animalHandoverRecordService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody AnimalHandoverRecord record) {
        return toJson(animalHandoverRecordService.create(record));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody AnimalHandoverRecord record) {
        return toJson(animalHandoverRecordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        animalHandoverRecordService.delete(id);
    }

    // переводим объект в json для ответа
    private Map<String, Object> toJson(AnimalHandoverRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("applicationId", record.getApplicationId());
        map.put("preparedAt", record.getPreparedAt());
        map.put("confirmedAt", record.getConfirmedAt());
        map.put("status", record.getStatus());
        return map;
    }
}
