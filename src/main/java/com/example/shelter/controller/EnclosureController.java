package com.example.shelter.controller;

import com.example.shelter.model.Enclosure;
import com.example.shelter.service.EnclosureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enclosures")
public class EnclosureController {

    @Autowired
    private EnclosureService enclosureService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Enclosure enclosure : enclosureService.getAll()) {
            result.add(toJson(enclosure));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id) {
        return toJson(enclosureService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Enclosure enclosure) {
        return toJson(enclosureService.create(enclosure));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Enclosure enclosure) {
        return toJson(enclosureService.update(id, enclosure));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        enclosureService.delete(id);
    }

    // переводим объект в json для ответа
    private Map<String, Object> toJson(Enclosure enclosure) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", enclosure.getId());
        map.put("allowedSpecies", enclosure.getAllowedSpecies());
        map.put("capacity", enclosure.getCapacity());
        return map;
    }
}
