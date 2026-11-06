package com.example.shelter.controller;
import com.example.shelter.model.*; import com.example.shelter.service.*; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.LinkedHashMap; import java.util.Map;
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController @RequestMapping("/api/operations")
public class BusinessOperationsController {
 private final AnimalService animals; private final AdoptionApplicationService applications; private final AnimalHandoverRecordService handovers; private final EnclosureService enclosures;
 public BusinessOperationsController(AnimalService animals, AdoptionApplicationService applications, AnimalHandoverRecordService handovers, EnclosureService enclosures){this.animals=animals;this.applications=applications;this.handovers=handovers;this.enclosures=enclosures;}
 @PostMapping("/animals/{animalId}/place/{enclosureId}") public Map<String,Object> place(@PathVariable Long animalId,@PathVariable Long enclosureId){Animal patch=new Animal(); patch.setEnclosureId(enclosureId); Animal a=animals.update(animalId,patch); return result("Животное размещено в вольере",a.getId(),a.getEnclosureId());}
 @PostMapping("/applications/{applicationId}/approve") public AdoptionApplication approve(@PathVariable Long applicationId){AdoptionApplication patch=new AdoptionApplication();patch.setStatus(ApplicationStatus.APPROVED);return applications.update(applicationId,patch);}
 @PostMapping("/applications/{applicationId}/reject") public AdoptionApplication reject(@PathVariable Long applicationId){AdoptionApplication patch=new AdoptionApplication();patch.setStatus(ApplicationStatus.REJECTED);return applications.update(applicationId,patch);}
 @PostMapping("/applications/{applicationId}/prepare-handover") @ResponseStatus(HttpStatus.CREATED) public AnimalHandoverRecord prepare(@PathVariable Long applicationId){AnimalHandoverRecord r=new AnimalHandoverRecord();r.setApplicationId(applicationId);return handovers.create(r);}
 @PostMapping("/handovers/{handoverId}/confirm") public AnimalHandoverRecord confirm(@PathVariable Long handoverId){AnimalHandoverRecord r=new AnimalHandoverRecord();r.setStatus(HandoverStatus.CONFIRMED);return handovers.update(handoverId,r);}
 private Map<String,Object> result(String message,Object id,Object relatedId){Map<String,Object> m=new LinkedHashMap<>();m.put("message",message);m.put("id",id);m.put("relatedId",relatedId);return m;}
}
