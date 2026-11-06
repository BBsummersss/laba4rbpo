package com.example.shelter.repository;
import com.example.shelter.model.AdoptionApplication; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface AdoptionApplicationJpaRepository extends JpaRepository<AdoptionApplication,Long> { List<AdoptionApplication> findByAnimalId(Long animalId); List<AdoptionApplication> findByApplicantId(Long applicantId); }
