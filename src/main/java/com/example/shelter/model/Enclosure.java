package com.example.shelter.model;
import jakarta.persistence.*; import java.util.ArrayList; import java.util.List;
@Entity @Table(name="enclosures") @org.hibernate.annotations.Check(constraints="capacity > 0")
public class Enclosure {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ElementCollection @CollectionTable(name="enclosure_allowed_species", joinColumns=@JoinColumn(name="enclosure_id", foreignKey=@ForeignKey(name="fk_species_enclosure"))) @Column(name="species", nullable=false, length=80) private List<String> allowedSpecies;
 @Column(nullable=false) private Integer capacity;
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public List<String> getAllowedSpecies(){return allowedSpecies;} public void setAllowedSpecies(List<String> allowedSpecies){this.allowedSpecies=allowedSpecies==null?null:new ArrayList<>(allowedSpecies);} public Integer getCapacity(){return capacity;} public void setCapacity(Integer capacity){this.capacity=capacity;}
}
