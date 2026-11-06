package com.example.shelter.repository;
import com.example.shelter.model.AnimalHandoverRecord; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface AnimalHandoverRecordJpaRepository extends JpaRepository<AnimalHandoverRecord,Long> { List<AnimalHandoverRecord> findByApplicationId(Long applicationId); }
