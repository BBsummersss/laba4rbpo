package com.example.shelter.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="animal_handover_records")
public class AnimalHandoverRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="application_id",nullable=false,unique=true) private Long applicationId;
 @Column(name="prepared_at",nullable=false) private LocalDateTime preparedAt;
 @Column(name="confirmed_at") private LocalDateTime confirmedAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private HandoverStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="application_id",insertable=false,updatable=false,foreignKey=@ForeignKey(name="fk_handover_application")) private AdoptionApplication application;
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getApplicationId(){return applicationId;} public void setApplicationId(Long applicationId){this.applicationId=applicationId;} public LocalDateTime getPreparedAt(){return preparedAt;} public void setPreparedAt(LocalDateTime preparedAt){this.preparedAt=preparedAt;} public LocalDateTime getConfirmedAt(){return confirmedAt;} public void setConfirmedAt(LocalDateTime confirmedAt){this.confirmedAt=confirmedAt;} public HandoverStatus getStatus(){return status;} public void setStatus(HandoverStatus status){this.status=status;}
}
