package com.example.shelter.model;
import jakarta.persistence.*;
@Entity @Table(name="adoption_applications")
public class AdoptionApplication {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="animal_id",nullable=false) private Long animalId;
 @Column(name="applicant_id",nullable=false) private Long applicantId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private ApplicationStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="animal_id",insertable=false,updatable=false,foreignKey=@ForeignKey(name="fk_application_animal")) private Animal animal;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="applicant_id",insertable=false,updatable=false,foreignKey=@ForeignKey(name="fk_application_user")) private User applicant;
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getAnimalId(){return animalId;} public void setAnimalId(Long animalId){this.animalId=animalId;} public Long getApplicantId(){return applicantId;} public void setApplicantId(Long applicantId){this.applicantId=applicantId;} public ApplicationStatus getStatus(){return status;} public void setStatus(ApplicationStatus status){this.status=status;}
}
