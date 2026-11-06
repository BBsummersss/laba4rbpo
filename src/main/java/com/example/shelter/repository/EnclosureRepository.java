package com.example.shelter.repository;
import com.example.shelter.model.Enclosure; import org.springframework.stereotype.Repository; import java.util.List;
@Repository public class EnclosureRepository { private final EnclosureJpaRepository dao; public EnclosureRepository(EnclosureJpaRepository dao){this.dao=dao;} public List<Enclosure> findAll(){return dao.findAll();} public Enclosure findById(Long id){return dao.findById(id).orElse(null);} public Enclosure save(Enclosure x){return dao.save(x);} public void deleteById(Long id){dao.deleteById(id);} }
