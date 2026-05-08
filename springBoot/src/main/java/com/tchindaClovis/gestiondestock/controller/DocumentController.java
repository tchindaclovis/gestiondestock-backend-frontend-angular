package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.DocumentApi;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController implements DocumentApi {

}

//@RequestMapping(value = "/document")
//public class DocumentController {
//
//    public DocumentController() {
//        super();
//        this.clazzE = Document.class;
//        this.clazzD = DocumentDto.class;
//        this.clazzR = DocumentDto.class;
//    }
//
//    @Autowired
//    DocumentService<Document> service;
//    Logger logger = Logger.getLogger(DocumentController.class);
//
//    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
//    public ResponseEntity<DocumentDto> findEntity(@PathVariable Long id,
//                                                  @RequestParam(value = "lang", required = true) String lang) {
//        return super.findEntity(id, lang);
//    }
//
//    @RequestMapping(value = "/all", method = RequestMethod.GET)
//    public ResponseEntity<BaseResultSet<Long, Document, DocumentDto>> findAll(
//            @RequestParam(value = "lang", required = true) String lang) {
//        return super.findAll(lang);
//    }
//
//    @Override
//    @RequestMapping(value = "/all", method = RequestMethod.POST)
//    public ResponseEntity<BaseResultSet<Long, Document, DocumentDto>> findAll(
//            @RequestBody List<SearchCriteria> criterias, @RequestParam(value = "lang", required = true) String lang) {
//        return super.findAll(criterias, lang);
//    }
//
//    @RequestMapping(value = "/transferer", method = RequestMethod.POST)
//    public ResponseEntity<BaseResultSet<Long, Document, DocumentDto>> transferer(@RequestBody List<Long> ids,
//                                                                                 @RequestParam(value = "destinataire", required = true) String destinataire,
//                                                                                 @RequestHeader("user") String user) throws Exception {
//        result = service.transferChezAutreManager(ids, getConnectedUser(user), destinataire);
//        try {
//            evaluateServiceResults("", true);
//            return new ResponseEntity<BaseResultSet<Long, Document, DocumentDto>>(resourcesR, STATUS);
//        } catch (Exception e) {
//            evaluateException(e, true);
//            return new ResponseEntity<BaseResultSet<Long, Document, DocumentDto>>(resourcesR, STATUS);
//        }
//    }
//
//    @RequestMapping(value = "/page", method = RequestMethod.POST)
//    public ResponseEntity<BaseResultSet<Long, Document, DocumentDto>> findPage(
//            @RequestBody List<SearchCriteria> criterias, @RequestParam(value = "lang", required = true) String lang,
//            @RequestParam(value = "page", required = true) int page,
//            @RequestParam(value = "size", required = true) int size, @RequestHeader("user") String user,
//            @RequestHeader("exercice") String exercice) {
//        SearchCriteria critExercice = new SearchCriteria("exercice", exercice, ESearchOperationType.EQUAL, true);
//        criterias.add(critExercice);
//        result = getService().findPage(service.getElementManagerCriteria(criterias, getConnectedUser(user)), lang, page,
//                size);
//        try {
//            evaluateServiceResults(lang, true);
//            return new ResponseEntity<BaseResultSet<Long, Document, DocumentDto>>(resourcesR, STATUS);
//        } catch (Exception e) {
//            evaluateException(e, true);
//            return new ResponseEntity<BaseResultSet<Long, Document, DocumentDto>>(resourcesR, STATUS);
//        }
//    }
//
//    @RequestMapping(value = "/create", method = RequestMethod.POST)
//    public ResponseEntity<DocumentDto> create(@RequestBody DocumentDto dto,
//                                              @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        return super.create(new Document(), dto, lang, user);
//    }
//
//    @RequestMapping(value = "/update", method = RequestMethod.PUT)
//    public ResponseEntity<DocumentDto> update(@RequestBody DocumentDto dto,
//                                              @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        return super.update(dto, lang, user);
//    }
//
//    @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
//    public ResponseEntity<DocumentDto> delete(@PathVariable Long id,
//                                              @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        return super.delete(id, lang, user);
//    }
//
//    @RequestMapping(value = "/sexes", method = RequestMethod.GET)
//    public ResponseEntity<Set<EnumDTO>> getSexes() {
//        return new ResponseEntity<Set<EnumDTO>>(BaseEnumWS.buildEnum(ESexe.values()), HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/initialiser", method = RequestMethod.POST)
//    public ResponseEntity<DocumentDto> initialiser(@RequestBody List<Long> ids,
//                                                   @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        try {
//            result = service.reinitialiser(getConnectedUser(user), ids, lang);
//            evaluateServiceResults(lang, false);
//            return new ResponseEntity<DocumentDto>(this.resource, STATUS);
//        } catch (Exception e) {
//            evaluateException(e);
//            return new ResponseEntity<DocumentDto>((DocumentDto) this.resourceR, STATUS);
//        }
//    }
//
//    @RequestMapping(value = "/validate", method = RequestMethod.POST)
//    public ResponseEntity<DocumentDto> validate(@RequestBody List<Long> ids,
//                                                @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        try {
//            result = service.validationManager(getConnectedUser(user), ids, null, EActionDocument.VALIDATION, lang);
//            evaluateServiceResults(lang, false);
//            return new ResponseEntity<DocumentDto>(this.resource, STATUS);
//        } catch (Exception e) {
//            evaluateException(e);
//            return new ResponseEntity<DocumentDto>((DocumentDto) this.resourceR, STATUS);
//        }
//    }
//
//    @RequestMapping(value = "/validateRH", method = RequestMethod.POST)
//    public ResponseEntity<DocumentDto> validateRH(@RequestBody List<Long> ids,
//                                                  @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        try {
//            result = service.validationRH(getConnectedUser(user), ids, null, EActionDocument.TRANSFERT, lang);
//            evaluateServiceResults(lang, false);
//            return new ResponseEntity<DocumentDto>(this.resource, STATUS);
//        } catch (Exception e) {
//            evaluateException(e);
//            return new ResponseEntity<DocumentDto>((DocumentDto) this.resourceR, STATUS);
//        }
//    }
//
//    @RequestMapping(value = "/reject", method = RequestMethod.POST)
//    public ResponseEntity<DocumentDto> reject(@RequestBody ObjectMappRejetMotif data,
//                                              @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        try {
//            result = service.rejetManager(getConnectedUser(user), data, null, EActionDocument.VALIDATION, lang);
//            evaluateServiceResults(lang, false);
//            return new ResponseEntity<DocumentDto>(this.resource, STATUS);
//        } catch (Exception e) {
//            evaluateException(e);
//            return new ResponseEntity<DocumentDto>((DocumentDto) this.resourceR, STATUS);
//        }
//    }
//
//    @RequestMapping(value = "/rejectRH", method = RequestMethod.POST)
//    public ResponseEntity<DocumentDto> rejectRH(@RequestBody ObjectMappRejetMotif data,
//                                                @RequestParam(value = "lang", required = true) String lang, @RequestHeader("user") String user) {
//        try {
//            result = service.rejetRH(getConnectedUser(user), data, null, EActionDocument.TRANSFERT, lang);
//            evaluateServiceResults(lang, false);
//            return new ResponseEntity<DocumentDto>(this.resource, STATUS);
//        } catch (Exception e) {
//            evaluateException(e);
//            return new ResponseEntity<DocumentDto>((DocumentDto) this.resourceR, STATUS);
//        }
//    }
//
//    @Override
//    public Logger getLOGGER() {
//        return logger;
//    }
//
//    @Override
//    public IBaseService<Long, Document> getService() {
//        return service;
//    }
//
//    @RequestMapping(value = "/etats", method = RequestMethod.GET)
//    public ResponseEntity<Set<EnumDTO>> findEntity() {
//        return new ResponseEntity<Set<EnumDTO>>(BaseEnumWS.buildEnum(EEtatDocument.values()), HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/types", method = RequestMethod.GET)
//    public ResponseEntity<Set<EnumDTO>> findEntityType() {
//        return new ResponseEntity<Set<EnumDTO>>(BaseEnumWS.buildEnum(ETypeDocument.values()), HttpStatus.OK);
//    }
//}
