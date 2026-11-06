package com.example.shelter.repository;
import com.example.shelter.model.Animal; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface AnimalJpaRepository extends JpaRepository<Animal,Long> { List<Animal> findByEnclosureId(Long enclosureId); }
