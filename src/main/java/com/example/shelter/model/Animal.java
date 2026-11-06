package com.example.shelter.model;
import jakarta.persistence.*;
@Entity @Table(name="animals") @org.hibernate.annotations.Check(constraints="age >= 0")
public class Animal {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String name;
 @Column(nullable=false,length=80) private String species;
 @Column(nullable=false) private Integer age;
 @Enumerated(EnumType.STRING) @Column(name="placement_status",nullable=false,length=30) private PlacementStatus placementStatus;
 @Column(name="enclosure_id") private Long enclosureId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="enclosure_id",insertable=false,updatable=false,foreignKey=@ForeignKey(name="fk_animal_enclosure")) private Enclosure enclosure;
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getName(){return name;} public void setName(String name){this.name=name;} public String getSpecies(){return species;} public void setSpecies(String species){this.species=species;} public Integer getAge(){return age;} public void setAge(Integer age){this.age=age;} public PlacementStatus getPlacementStatus(){return placementStatus;} public void setPlacementStatus(PlacementStatus placementStatus){this.placementStatus=placementStatus;} public Long getEnclosureId(){return enclosureId;} public void setEnclosureId(Long enclosureId){this.enclosureId=enclosureId;this.enclosure=null;}
}
