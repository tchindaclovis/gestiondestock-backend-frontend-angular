package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.services.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j  //Génère automatiquement l'objet log pour écrire dans la console.
//@Primary  //Désigne le "favori" quand il y a plusieurs Beans du même type
public class DocumentServiceImpl implements DocumentService {

}







//public class DocumentServiceImpl<E extends Document>  implements DocumentService<E> {
//
//    public Class<E> clazz;
//
//    Logger logger = Logger.getLogger(DocumentServiceImpl.class);
//
//    @Autowired
//    protected DocumentRepository<E> repository;
//
//    @Autowired
//    IExerciceService exerciceService;
//
//    @Autowired
//    IEvenementService evenService;
//
//    @Autowired
//    EmployeRepo EmployeRepo;
//
//    @Autowired
//    protected DocRealisationRepository docRealisationRepository;
//
//
////    @Autowired
////    private DemandeDerogationRepository derogationRepository;
//
////    @Autowired
////    private IDemandeDerogationService derogationService;
//
//    @Autowired
//    private TransmissionRepository transmissionRepository;
//
//    @Autowired
//    ITransmissionService transmissionService;
//
//    @Autowired
//    private EmployeRepository employeRepository;
//
//    @Autowired
//    private EmployeModifiableRepository employeModifiableRepo;
//
//
//    @Autowired
//    IEmployeModifiableService employeModifiableService;
//
//    @Autowired
//    BesoinRepository besoinRepository;
//
//    @Autowired
//    IBesoinService besoinService;
//
//
//    @Autowired
//    INotificationService notificationService;
//
//    @Override
//    protected Logger getLogger() {
//        return logger;
//    }
//
//    @Override
//    protected List<ValidatorResult> validateEntity(E record, EOperation operation) {
//        List<ValidatorResult> vr = new ArrayList<ValidatorResult>();
//
//        return vr;
//    }
//
//    @Override
//    protected List<ValidatorResult> validateAllEntities(Set<E> record, EOperation operation) {
//        return new ArrayList<>(0);
//    }
//
//    public DocumentRepository<E> getRepository() {
//        return repository;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Class<E> getClazz() {
//        if (clazz != null)
//            return clazz;
//        else {
//            //logger.error("clazz.Null.Setting.Document.Default.As.Class");
//            return (Class<E>) Document.class;
//        }
//    }
//
//    @Override
//    public void setClazz(Class<E> zz) {
//        this.clazz = zz;
//    }
//
//    public PageRequest getPage(int page, int size, Sort sort) {
//        PageRequest pageRequest = null;
//        if (sort == null) {
//            pageRequest = new PageRequest(page, size);
//        } else {
//            pageRequest = new PageRequest(page, size, sort);
//        }
//        return pageRequest;
//    }
//
//    @Override
//    public Result<E> findPageByExercice(String exercice, String lang) {
//        Result<E> serviceResult = createResult();
//        try {
//            Set<E> pageResult = repository.findByExercice(exercice);
//            serviceResult.getEntities().addAll(pageResult);
//            serviceResult.setTotalElements(pageResult.size());
//            serviceResult.setTotalPages(1);
//            serviceResult.setType(EReturnType.Page);
//            serviceResult = setResultDetails(serviceResult, lang);
//        } catch (Exception e) {
//            throwException("System.error", lang, null, e);
//        }
//        return serviceResult;
//    }
//
//    @Override
//    public Result<E> findPageByExerciceAndMatricule(String exercice, String matricule, String lang) {
//        Result<E> result = createResult();
//        try {
//            Set<E> pageResult = repository.findByExerciceAndProprietaire_Matricule(exercice, matricule);
//            result.getEntities().addAll(pageResult);
//            result.setTotalElements(pageResult.size());
//            result.setTotalPages(1);
//            result.setType(EReturnType.Page);
//            result = setResultDetails(result, lang);
//        } catch (Exception e) {
//            throwException("System.error", lang, null, e);
//        }
//        return result;
//    }
//
//    @Override
//    public Result<E> findPageByExerciceAndMatriculeAndPeriodeNumero(String exercice, String matricule, Integer numero) {
//        List<ValidatorResult> vr = new ArrayList<ValidatorResult>();
//        vr.add(new ValidatorResult(clazz, "method",
//                "Method.DocumentServiceImpl.findPageByExerciceAndMatriculeAndPeriodeNumero.not.implemented"));
//        return getInvalidResult(vr, DEFAULT_LANG);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Result<E> findEntityByCode(String code, String lang) {
//        Result<E> result = createResult();
//        try {
//            Document entity = repository.findByCode(code);
//            result.getEntities().add((E) entity);
//            result.setType(EReturnType.Entity);
//            result = setResultDetails(result, lang);
//        } catch (Exception e) {
//            throwException("System.error", lang, null, e);
//        }
//        return result;
//    }
//
//    @Override
//    public Result<E> sendToDerogation(User user, E document, String matDestinataire, EActionDerogation action,
//                                      String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//
//        Result<Exercice> exoRes = exerciceService.checkExercice(document.getExercice(), DEFAULT_LANG);
//        if (!exoRes.isValid()) {
//            result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//            return result;
//        }
//
//        // Je recharge l'object en question
//        //document = (E) repository.findOne(document.getId());
//        if (StringHelper.isNotEmpty(matDestinataire)) {
//            document.setDestinataire(matDestinataire);
//        }
//        if(document.getDateEnvoi() == null) {
//            document.setDateEnvoi(new Date());
//        }
//
//        document.setModifiable(false);
//        document.setEtat(EEtatDocument.DEROGATION);
//        switch (action) {
//            case ENVOI:
//                document.setNeedDeroEnvoi(true);
//                break;
//            case VALIDATION:
//                document.setNeedDeroValid(true);
//                document.setEtat(EEtatDocument.DEROGATION_VALIDATION);
//                break;
//            case REJET_VALIDATION:
//                document.setNeedDeroValid(true);
//                document.setEtat(EEtatDocument.DEROGATION_REJET);
//                break;
//            case TRANSFERT:
//            case REJET_TRANSFERT:
//                document.setNeedDeroTransfert(true);
//                break;
//            default:
//                break;
//        }
//        // Je mets à jout le document
//        document = (E) repository.saveAndFlush(document);
//
//        // J'enregistre dérogation
//        derogationService.create(user, new DemandeDerogation(document, action), langCode);
//
//        postEvent(user, document.getCode(), EActionDocument.DEROGATION, document.getProprietaire().getMatricule(),
//                matDestinataire, true);
//
//        String message = "Demande de dérogation pour (" + action.getDescription() + ") enregistrée";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//
//    @Override
//    public Result<E> sendToValidation(User user, E document, String matricule, String matDestinataire,
//                                      EActionDocument action, String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//        Result<Exercice> exoRes = exerciceService.checkExercice(document.getExercice(), DEFAULT_LANG);
//        if (!exoRes.isValid()) {
//            result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//            return result;
//        }
//        if (isNeedDerogation(document, action)) {
//            return sendToDerogation(user, document, matDestinataire, EActionDerogation.ENVOI, langCode);
//        }
//        // Je recharge l'object en question
//        // document = (E) repository.findOne(document.getId());
//        // Rechercher l'ancienne transmisson et la marqué comme traité
//        List<Transmission> oldTransmissions = transmissionRepository
//                .findByDocument_CodeAndDestinataires_MatriculeAndTraite(document.getCode(), matricule, false);
//        for (Transmission transmission : oldTransmissions) {
//            transmission.setTraite(true);
//            transmission.setDateReponse(new Date());
//            transmission = transmissionRepository.saveAndFlush(transmission);
//        }
//
//        // Action possible lors de l'envoi en validation
//        List<EActionDocument> actions = new ArrayList<>(0);
//        if (EActionDocument.DMDVALIDATION.equals(action)) {
//            actions.add(EActionDocument.ACTION_REJET);
//            actions.add(EActionDocument.VALIDATION);
//        } else if (EActionDocument.ACTION_REJET.equals(action)) {
//            actions.add(EActionDocument.DMDVALIDATION);
//        }
//
//        Employe destinataire = employeRepository.findByMatricule(matDestinataire);
//        // Liste de destinataires
//        List<Employe> destinataires = new ArrayList<>(0);
//        destinataires.add(destinataire);
//
//        Employe employe = employeRepository.findByMatricule(matricule);
//        // Je crée la nourvelle transmission
//        transmissionService.create(user, new Transmission(document, employe, action, destinataires, actions),
//                DEFAULT_LANG);
//
//        // Miseà jour du document
//        document.setEtat(EEtatDocument.ENCOURS);
//        document.setModifiable(false);
//        document.setDestinataire(destinataire.getMatricule());
//        document.setDateEnvoi(new Date());
//
//        // Je mets à jout le document
//        document = update(user, document, DEFAULT_LANG).getEntity();
//
//        // Je crée un évènement
//        postEvent(user, document.getCode(), action, matricule, matDestinataire);
//
//        String message = "Envoi en validation  enregistrée";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//    @Override
//    public boolean isNeedDerogation(E record, EActionDocument action) {
//        List<EActionDocument> actions = new ArrayList<>(0);
//        actions.add(EActionDocument.DEROGATION);
//        Date dateDuJour = new Date();
//        boolean result = true;
//        getLogger().info("Date du jour : " + dateDuJour);
//        // chargement du doc
//        if (EActionDocument.DMDVALIDATION.equals(action)) {
//            if(record.getDateButoireEnvoi() != null) {
//                getLogger().info("Date butoire envoi : " + record.getDateButoireEnvoi());
//                if (new SimpleDateFormat("dd/MM/yyyy").format(dateDuJour)
//                        .equals(new SimpleDateFormat("dd/MM/yyyy").format(record.getDateButoireEnvoi())))
//                    result = false;
//                else {
//                    if (dateDuJour.before(record.getDateButoireValidation()))
//                        result = false;
//                }
//            }else {
//                result = false;
//            }
//
//        } else if (EActionDocument.VALIDATION.equals(action) || (EActionDocument.ACTION_REJET.equals(action))) {
//            if(record.getDateButoireValidation() != null) {
//                getLogger().info("Date butoire validation : " + record.getDateButoireValidation());
//                if (new SimpleDateFormat("dd/MM/yyyy").format(dateDuJour)
//                        .equals(new SimpleDateFormat("dd/MM/yyyy").format(record.getDateButoireValidation())))
//                    result = false;
//                else {
//                    if (dateDuJour.before(record.getDateButoireValidation()))
//                        result = false;
//                }
//                if (EActionDocument.ACTION_REJET.equals(action) && record.isDeroValid())
//                    result = false;
//            }else {
//                result = false;
//            }
//
//
//        } else if (EActionDocument.TRANSFERT.equals(action) && record.getDateButoireTransfert()!=null) {
//            if(record.getDateButoireTransfert() != null) {
//                getLogger().info("Date butoire transfert : " + record.getDateButoireTransfert());
//
//                if (new SimpleDateFormat("dd/MM/yyyy").format(dateDuJour)
//                        .equals(new SimpleDateFormat("dd/MM/yyyy").format(record.getDateButoireTransfert())))
//                    result = false;
//                else {
//                    if (dateDuJour.before(record.getDateButoireTransfert()))
//                        result = false;
//                }
//            }else {
//                result = false;
//            }
//
//        }
//
//        return result;
//    }
//
//    @Override
//    public Result<E> transfert(User user, E record, String langCode) throws Exception {
//        List<E> records = new ArrayList<>();
//        records.add(record);
//        return transfert(user, records, langCode);
//    }
//
//    @Transactional
//    @Override
//    public Result<E> transfert(User user, List<E> records, String lang) throws Exception {
//        Result<E> result = createResult(lang);
//        Result<Exercice> exoRes = exerciceService.checkExercice(records.get(0).getExercice(), DEFAULT_LANG);
//        if (exoRes.isValid()) {
//            result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), lang);
//            return result;
//        }
//        for (E record : records) {
//            // Je recharge le record
//            //record = (E) repository.findByCode(record.getCode());
//            record.setDateTransfert(new Date());
//            record.setEtat(EEtatDocument.ATTENTE_RANSFERER);
//            record.setModifiable(false);
//            record = (E) repository.saveAndFlush(record);
//
//            // TODO Transfert du document
//            // transfertVersAltGRH(record);
//
//            // TODO Transfert vers ALTGRH
//            // postEvent(user, record.getCode(), EActionDocument.TRANSFERT,
//            // record.getProprietaire().getMatricule());
//        }
//
//        String message = "Validation réussi";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//
//        return result;
//    }
//
////    @Override
////    public Result<E> transfertDerogation(User user, E record, String langCode) throws Exception {
////        Result<E> serviceResult = createResult(langCode);
////        Result<Exercice> exoRes = exerciceService.checkExercice(record.getExercice(), DEFAULT_LANG);
////        if (exoRes.isValid()) {
////            serviceResult = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
////            return serviceResult;
////        }
////        // Je recharge le record
////        //record = (E) repository.findByCode(record.getCode());
////        record.setDateTransfert(new Date());
////        record.setModifiable(false);
////        record = (E) repository.saveAndFlush(record);
////        //
////        return sendToDerogation(user, record, null, EActionDerogation.TRANSFERT, langCode);
////    }
//
//    @Override
//    public Result<E> rejeter(User user, E record, String motif, String langCode) throws Exception {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
////    @Override
////    public Result<E> rejeterDerogation(User user, E record, String motif, String langCode) throws Exception {
////        // TODO Auto-generated method stub
////        return null;
////    }
//
////    @Override
////    public boolean isNeedDerogation(String codeDocument, EActionDocument action) {
////        E record = repository.findByCode(codeDocument);
////        return isNeedDerogation(record, action);
////    }
//
//    @Override
//    public Result<E> reinitialiser(User user, List<Long> ids, String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//        for (Long id : ids) {
//            E record = repository.getOne(id);
//            try {
//                Result<Exercice> exoRes = exerciceService.checkExercice(record.getExercice(), DEFAULT_LANG);
//                if (!exoRes.isValid()) {
//                    return super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//                }
//                // GenericServiceResult<E>'il y a une dérogation, je la marque
//                // comme
//                // rejeter
////                List<DemandeDerogation> derogations = derogationRepository.findListByDocument_Code(record.getCode());
////                if (derogations != null && !derogations.isEmpty()) {
////                    for (DemandeDerogation derogation : derogations) {
////                        derogation.setReponse(false);
////                        derogation.setTraite(true);
////                        derogation.setDateReponse(new Date());
////                        derogationRepository.saveAndFlush(derogation);
////                    }
////                }
//                // GenericServiceResult<E>'il y a une dérogation, je la marque
//                // comme
//                // rejeter
//                List<Transmission> transmissions = transmissionRepository.findListByCode(record.getCode());
//                if (transmissions != null && !transmissions.isEmpty()) {
//                    for (Transmission transmission : transmissions) {
//                        transmission.setDateReponse(new Date());
//                        transmission.setTraite(true);
//                        transmission.setNote("Réinitialisation du document");
//                        transmissionRepository.saveAndFlush(transmission);
//                    }
//                }
//                // Je recharge le record
//                record = (E) repository.findOne(id);
//                record.setDateTransfert(null);
//                record.setModifiable(true);
////                record.setNeedDeroEnvoi(false);
////                record.setDeroEnvoi(false);
////                record.setNeedDeroTransfert(false);
////                record.setDeroTransfert(false);
////                record.setNeedDeroValid(false);
////                record.setDeroValid(false);
////                record.setDernierMotifRejet("");
//                record.setDernierRejetteur("");
//                record.setDateDernierRejet(null);
//                record.setDestinataire("");
//                record.setEtat(EEtatDocument.ENREGISTRER);
//                record = (E) repository.saveAndFlush(record);
//
//                // Réinitialisation du document
//                postEvent(user, record.getCode(), EActionDocument.INITIALISATION,
//                        record.getProprietaire().getMatricule());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        String message = "Réinitialisation réussie";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//    @Override
//    public Result<E> reinitialiser(User user, E record, String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//        Result<Exercice> exoRes = exerciceService.checkExercice(record.getExercice(), DEFAULT_LANG);
//        if (exoRes.isValid()) {
//            return super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//        }
//        // GenericServiceResult<E>'il y a une dérogation, je la marque comme
//        // rejeter
////        List<DemandeDerogation> derogations = derogationRepository.findListByDocument_Code(record.getCode());
////        if (derogations != null && !derogations.isEmpty()) {
////            for (DemandeDerogation derogation : derogations) {
////                derogation.setReponse(false);
////                derogation.setTraite(true);
////                derogation.setDateReponse(new Date());
////                derogationRepository.saveAndFlush(derogation);
////            }
////        }
//        // GenericServiceResult<E>'il y a une dérogation, je la marque comme
//        // rejeter
//        List<Transmission> transmissions = transmissionRepository.findListByCode(record.getCode());
//        if (transmissions != null && !transmissions.isEmpty()) {
//            for (Transmission transmission : transmissions) {
//                transmission.setDateReponse(new Date());
//                transmission.setTraite(true);
//                transmission.setNote("Réinitialisation du document");
//                transmissionRepository.saveAndFlush(transmission);
//            }
//        }
//        // Je recharge le record
//        record = (E) repository.findOne(record.getId());
//        record.setDateTransfert(null);
//        record.setModifiable(true);
////        record.setNeedDeroEnvoi(false);
////        record.setDeroEnvoi(false);
////        record.setNeedDeroTransfert(false);
////        record.setDeroTransfert(false);
////        record.setNeedDeroValid(false);
////        record.setDeroValid(false);
//        record.setDernierMotifRejet("");
//        record.setDernierRejetteur("");
//        record.setDateDernierRejet(null);
//        record.setDestinataire("");
//        record.setEtat(EEtatDocument.ENREGISTRER);
//        record = (E) repository.saveAndFlush(record);
//
//        // Réinitialisation du document
//        postEvent(user, record.getCode(), EActionDocument.INITIALISATION, record.getProprietaire().getMatricule());
//        String message = "Réinitialisation réussie";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//    @Override
//    public Result<Document> transferChezAutreManager(List<Long> docs, User user,String matDestinataire) throws Exception {
//        Result<Document> result = new Result<>();
//        for(Long id: docs) {
//            E doc = repository.findOne(id);
//            sendToValidation(user, doc, EActionDocument.VALIDATION, doc.getProprietaire().getMatricule(), matDestinataire);
//        }
//        result.setMessage("Success");
//        result.setType(EReturnType.Set);
//        result.setValid(true);
//        return result;
//    }
//
//    private void postEvent(User user, String record, EActionDocument action, String proprietaire, String destinataire) {
//        postEvent(user, record, action, proprietaire, destinataire, false);
//    }
//
//    private void postEvent(User user, String record, EActionDocument action, String proprietaire) {
//        postEvent(user, record, action, proprietaire, null);
//    }
//
//    private void postEvent(User user, String record, EActionDocument action, String matriculeProp, String matDesti,
//                           boolean derogation) {
//        try {
//            // Rerchercher un utilisateur sur la base de son login
//            // TODO initialize user
//            User proprio = employeRepository.findUserByMatricule(matriculeProp);
//            String loginDest = null;
//            if (StringHelper.isNotEmpty(matDesti)) {
//                // TODO initialize user
//                User desti = employeRepository.findUserByMatricule(matDesti);
//                loginDest = desti.getLogin();
//            }
//            Evenement event = new Evenement(record, action, proprio.getLogin(), loginDest, derogation);
//            evenService.create(user, event, DEFAULT_LANG);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
//    }
//
//    public void sendToValidation(User user, E document, EActionDocument action, String matricule,
//                                 String matDestinataire) throws Exception {
//        // Rechercher l'ancienne transmisson et la marqué comme traité
//        List<Transmission> oldTransmissions = transmissionRepository
//                .findByDocument_CodeAndDestinataires_MatriculeAndTraite(document.getCode(), matricule, false);
//        for (Transmission transmission : oldTransmissions) {
//            transmission.setTraite(true);
//            transmission.setDateReponse(new Date());
//            transmission = transmissionRepository.saveAndFlush(transmission);
//        }
//
//        // Action possible lors de l'envoi en validation
//        List<EActionDocument> actions = new ArrayList<>(0);
//        if (EActionDocument.DMDVALIDATION.equals(action)) {
//            actions.add(EActionDocument.ACTION_REJET);
//            actions.add(EActionDocument.VALIDATION);
//
//        } else if (EActionDocument.ACTION_REJET.equals(action)) {
//            actions.add(EActionDocument.DMDDEROGATION);
//        }
//
//        Employe destinataire = employeRepository.findByMatricule(matDestinataire);
//        // Liste de destinataires
//        List<Employe> destinataires = new ArrayList<>(0);
//        destinataires.add(destinataire);
//
//        Employe employe = employeRepository.findByMatricule(matricule);
//        // Je crée la nourvelle transmission
//        transmissionService.create(user, new Transmission(document, employe, action, destinataires, actions),
//                DEFAULT_LANG);
//
//        // Miseà jour du document
//        document.setEtat(EEtatDocument.ENCOURS);
//        document.setModifiable(false);
//        document.setDestinataire(destinataire.getMatricule());
//        document.setDateEnvoi(new Date());
//
//        // Je mets à jout le document
//        document = this.update(user, document, "").getEntity();
//        // Action d'envoi en validation
//        postEvent(user, document.getCode(), action, matricule, matDestinataire);
//    }
//
//    @Override
//    public Result<E> findByExerciceAndProprietaireMatricule(String exercise, String matricule) {
//        // E e =
//        // getRepository().findByExerciceAndProprietaireMatricule(exercise,
//        // matricule);
//        return null;
//    }
//
//    @Override
//    public Result<E> findByExerciceAndProprietaireMatricule(String exercise, String matricule, String lang) {
//
//        return null;
//    }
//
//    @Override
//    public List<SearchCriteria> getElementManagerCriteria(List<SearchCriteria> criterias, User user) {
//        Employe employe = EmployeRepo.findByUserId(user.getId());
//        SearchCriteria elements = null;
//        SearchCriteria destinataire = null;
//        for (SearchCriteria criteria : criterias) {
//            if (criteria.getKey().contentEquals("elements")) {
//                elements = criteria;
//                break;
//            }
//        }
//        for (SearchCriteria criteria : criterias) {
//            if (criteria.getKey().contentEquals("destinataire")) {
//                destinataire = criteria;
//                break;
//            }
//        }
//        if (elements != null) {
//            criterias.remove(elements);
//            if (elements.getValue() != null) {
//                elements.setKey("proprietaire.managers.matricule");
//                elements.setAliasKey("managers.matricule");
//                elements.setValue(employe.getMatricule());
//                elements.setType(ESearchOperationType.EQUAL);
//                elements.setApplyAnd(true);
//                criterias.add(elements);
//            }
//        }
//        if (destinataire != null) {
//            criterias.remove(destinataire);
//            if (destinataire.getValue() != null) {
//                destinataire.setKey("destinataire");
//                destinataire.setAliasKey("destinataire");
//                destinataire.setValue(employe.getMatricule());
//                destinataire.setType(ESearchOperationType.EQUAL);
//                destinataire.setApplyAnd(true);
//                criterias.add(destinataire);
//            }
//        }
//        return criterias;
//    }
//
//    @Override
//    public Result<E> validationRH(User user, List<Long> ids, String matricule, EActionDocument action, String langCode)
//            throws Exception {
//        Result<E> result = createResult(langCode);
//        List<E> documents = repository.findByIdIsIn(ids);
//        for (E doc : documents) {
//            if (doc.getEtat() != EEtatDocument.VALIDER) {
//                result = super.getInvalidResult(null, langCode);
//                result.setMessage("Aucune demande de validation RH en cours pour ce document :" + doc.getCode());
//                return result;
//            }
//            Result<Exercice> exoRes = exerciceService.checkExercice(doc.getExercice(), DEFAULT_LANG);
//            if (!exoRes.isValid()) {
//                result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//                return result;
//            } else {
//                // Je recharge l'object en question
//                doc = (E) repository.findOne(doc.getId());
//                // Rechercher l'ancienne transmisson et la marqué comme traité
//                List<Transmission> oldTransmissions = transmissionRepository
//                        .findByDocument_CodeAndDestinataires_MatriculeAndTraite(doc.getCode(), matricule, false);
//                for (Transmission transmission : oldTransmissions) {
//                    transmission.setTraite(true);
//                    transmission.setDateReponse(new Date());
//                    transmission = transmissionRepository.saveAndFlush(transmission);
//                }
//
//
//
//                // Verifie qu'il s'agit d'une expression de besoin
//                if(doc.getType().equals(ETypeDocument.besoin)) {
//                    Besoin besoin = besoinRepository.findByCode(doc.getCode());
//
//                    // Verifie si c'est possible de valider l'expression de besoin
//                    List<ValidatorResult> resultBesoin =  besoinService.checkIfPossible(besoin);
//                    if(resultBesoin.isEmpty()) {
//                        //-- besoin.setEtatBesoin(EEtatBesoin.EnCours);
//                        besoin.setDateValidationBesoin(new Date());
//                    } else {
//                        String message = "";
//                        for(ValidatorResult validator : resultBesoin) {
//                            message += message + validator.getMessage() + "\n";
//                        }
//                        result.setType(EReturnType.Entity);
//                        result.setValid(false);
//                        result.setMessage(message);
//                        return result;
//                    }
////					doc = update(user, doc, DEFAULT_LANG).getEntity();
//                }
//
//
//
////                // Verifie si c'est possible de valider la demande de modification du profile
////                if(doc.getType().equals(ETypeDocument.modification_infos_profile)) {
////                    EmployeModifiable em = employeModifiableRepo.findOne(doc.getId());
////                    if (em.getEtatEmployeModifiable().equals(EEtatEmployeModifiable.EnCours)) {
////
////                        if (em.getTitre()!= null) {
////                            em.getEmploye().setTitreEmploye(em.getTitre());
////                        }
////
////                        if (em.getNoms()!= null) {
////                            em.getEmploye().setNoms(em.getNoms());
////                        }
////                        if(em.getPrenom() != null) {
////                            em.getEmploye().setPrenom(em.getPrenom());
////                        }
////
////                        if (em.getDateActeNaissance()!= null) {
////                            em.getEmploye().setDateActeNaissance(em.getDateActeNaissance());
////                        }
////
////                        if (em.getDateNaissance()!= null) {
////                            em.getEmploye().setDateNaissance(em.getDateNaissance());
////                        }
////
////                        if (em.getNomPere()!= null) {
////                            em.getEmploye().setNomPere(em.getNomPere());
////                        }
////
////                        if (em.getNomMere()!=null) {
////                            em.getEmploye().setNomMere(em.getNomMere());
////                        }
////
////                        if (em.getStatutMatrimonial()!=null) {
////                            em.getEmploye().setStatutMatrimonial(em.getStatutMatrimonial());
////                        }
////
////                        if (em.getMatriculeCnps()!=null) {
////                            em.getEmploye().setMatriculeCnps(em.getMatriculeCnps());
////                        }
////
////                        if (em.getNumContribuable()!=null) {
////                            em.getEmploye().setNumContribuable(em.getNumContribuable());
////                        }
////
////                        if (em.getLieuCnps()!=null) {
////                            em.getEmploye().setLieuCnps(em.getLieuCnps());
////                        }
////
////                        if (em.getEmployeur()!=null) {
////                            em.getEmploye().setEmployeur(em.getEmployeur());
////                        }
////
////                        if (em.getDateCnps()!=null) {
////                            em.getEmploye().setDateCnps(em.getDateCnps());
////                        }
////                        if (em.getCv()!=null) {
////                            em.getEmploye().setCv(em.getCv());
////                        }
////
////                        if (em.getSexe()!=null) {
////                            em.getEmploye().setSexe(em.getSexe());
////                        }
////                        if (em.getLieuNaissance()!=null) {
////                            em.getEmploye().setLieuNaissance(em.getLieuNaissance());
////                        }
////                        if (em.getRegion()!=null) {
////                            em.getEmploye().setRegion(em.getRegion());
////                        }
////
////                        if (em.getRegionNaissance()!=null) {
////                            em.getEmploye().setRegionNaissance(em.getRegionNaissance());
////                        }
////
////                        if (em.getNumeroActeNaissance()!=null) {
////                            em.getEmploye().setNumeroActeNaissance(em.getNumeroActeNaissance());
////                        }
////
////                        if (em.getPhotoByteArray()!=null) {
////                            em.getEmploye().setPhotoByteArray(em.getPhotoByteArray());
////                        }
////
////                        if(em.getPaysNaissance() != null) {
////                            em.getEmploye().setPaysNaissance(em.getPaysNaissance());
////                        }
////
////                        if (em.getDocuments()!=null) {
////                            em.getEmploye().getDocuments().clear();
////                            em.getEmploye().getDocuments().addAll(em.getDocuments());
////                        }
////                        if (em.getPieces()!=null) {
////                            em.getEmploye().getPieces().clear();
////                            em.getEmploye().getPieces().addAll(em.getPieces());
////                        }
////                        if (em.getContacts()!=null) {
////                            em.getEmploye().getContacts().clear();
////                            em.getEmploye().getContacts().addAll(em.getContacts());
////                        }
////                        if (em.getContactsUrgence()!=null) {
////                            em.getEmploye().getContactsUrgence().clear();
////                            em.getEmploye().getContactsUrgence().addAll(em.getContactsUrgence());
////                        }
////                        if (em.getDiplomes()!=null) {
////                            em.getEmploye().getDiplomes().clear();
////                            em.getEmploye().getDiplomes().addAll(em.getDiplomes());
////                        }
////                        if(em.getEnfants()!=null) {
////                            em.getEmploye().getEnfants().clear();
////                            em.getEmploye().getEnfants().addAll(em.getEnfants());
////                        }
////                        em.setEtat(EEtatDocument.VALIDERRH);
////                        em.setEtatEmployeModifiable(EEtatEmployeModifiable.Valider);
////                        em.setDateValidationDemandeMotifProfile(new  Date());
////                    }
////                }
//
//                doc.setEtat(EEtatDocument.VALIDERRH);
//                doc.setModifiable(false);
//                doc.setDateValidation(new Date());
//
//                // Je mets à jout le document
//                doc = update(user, doc, DEFAULT_LANG).getEntity();
//
//
//            }
//        }
//
//
//        String message = "Documents validés";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//
//    @Override
//    public Result<E> validationManager(User user, List<Long> ids, String matricule, EActionDocument action,
//                                       String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//        List<E> documents = repository.findByIdIsIn(ids);
//        for (E doc : documents) {
//            if (doc.getEtat() != EEtatDocument.ENCOURS) {
//                result = super.getInvalidResult(null, langCode);
//                result.setMessage("Aucune demande de validation en cours pour ce document :" + doc.getCode());
//                return result;
//            }
//            if (isNeedDerogation(doc, action)) {
//                sendToDerogation(user, doc, null, EActionDerogation.VALIDATION, langCode);
//                continue;
//            }
//            Result<Exercice> exoRes = exerciceService.checkExercice(doc.getExercice(), DEFAULT_LANG);
//            if (!exoRes.isValid()) {
//                result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//                return result;
//            } else {
//                // Je recharge l'object en question
//                doc = (E) repository.findOne(doc.getId());
//                // Rechercher l'ancienne transmisson et la marqué comme traité
//                List<Transmission> oldTransmissions = transmissionRepository
//                        .findByDocument_CodeAndDestinataires_MatriculeAndTraite(doc.getCode(), matricule, false);
//                for (Transmission transmission : oldTransmissions) {
//                    transmission.setTraite(true);
//                    transmission.setDateReponse(new Date());
//                    transmission = transmissionRepository.saveAndFlush(transmission);
//                }
//                doc.setEtat(EEtatDocument.VALIDER);
//                doc.setModifiable(false);
//                doc.setDateValidation(new Date());
//                // Je mets à jout le document
//                doc = update(user, doc, DEFAULT_LANG).getEntity();
//
//                //Gestion des notifications
//                if(doc.getType().equals(ETypeDocument.conge)) {
//                    Document docu = doc;
//                    ExecutorService executorService = Executors.newSingleThreadExecutor();
//                    executorService.submit(() -> {
//                        try {
//                            notificationService.notificationRhDemandeConge((DemandeConge)docu);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//                    executorService.shutdown();
//
//                }
//            }
//        }
//        String message = "Documents validés";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//    @Override
//    public Result<E> rejetRH(User user, ObjectMappRejetMotif data, String matricule, EActionDocument action,
//                             String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//        List<E> documents = repository.findByIdIsIn(data.getIds());
//        Employe manager = employeRepository.findByUser(user);
//        for (E doc : documents) {
//            Result<Exercice> exoRes = exerciceService.checkExercice(doc.getExercice(), DEFAULT_LANG);
//            if (doc.getEtat() != EEtatDocument.VALIDER) {
//                result = super.getInvalidResult(null, langCode);
//                result.setMessage("Aucune demande de validation RH en cours pour ce document :" + doc.getCode());
//                return result;
//            }
//            if (!exoRes.isValid()) {
//                result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//                return result;
//            } else {
//                // Je recharge l'object en question
//                doc = (E) repository.findOne(doc.getId());
//                // Rechercher l'ancienne transmisson et la marqué comme traité
//                List<Transmission> oldTransmissions = transmissionRepository
//                        .findByDocument_CodeAndDestinataires_MatriculeAndTraite(doc.getCode(), matricule, false);
//                for (Transmission transmission : oldTransmissions) {
//                    transmission.setTraite(true);
//                    transmission.setDateReponse(new Date());
//                    transmission = transmissionRepository.saveAndFlush(transmission);
//                }
//                doc.setEtat(EEtatDocument.REJETDERH);
//                doc.setModifiable(true);
//                doc.setDateDernierRejet(new Date());
//                doc.setDernierMotifRejet(data.getMotif());
//                doc.setDernierRejetteur((manager.getNoms() != null ? manager.getNoms() : "")
//                        + (manager.getPrenom() != null ? manager.getPrenom() : ""));
//
//
//                if(doc.getType().equals(ETypeDocument.besoin)) {
//                    Besoin besoin = besoinRepository.findByCode(doc.getCode());
//                    besoin.setEtatBesoin(EEtatBesoin.Rejette);
//                }
//
//                // Fin traitement
//
//                // traitement particulier pour le cas des employe modifiable non validés
//                if(doc.getType().equals(ETypeDocument.modification_infos_profile)) {
//                    EmployeModifiable em = employeModifiableRepo.findOne(doc.getId());
//                    if (em != null) {
//                        em.setEtatEmployeModifiable(EEtatEmployeModifiable.Rejette);
//                    }
//                }
//                // Fin traitement
//
//                // Je mets à jour le document
//                doc = update(user, doc, DEFAULT_LANG).getEntity();
//
//                //Gestion des notifications
//                if(doc.getType().equals(ETypeDocument.conge)) {
//                    Document docu = doc;
//                    ExecutorService executorService = Executors.newSingleThreadExecutor();
//                    executorService.submit(() -> {
//                        try {
//                            notificationService.notificationEmployeRejetDemandeConge((DemandeConge)docu);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//                    executorService.shutdown();
//
//                }
//            }
//        }
//        // // Je crée un évènement
//        // postEvent(user, document.getCode(), action, matricule,
//        // matDestinataire);
//
//        String message = "Document rejeté";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//
//    @Override
//    public Result<E> rejetManager(User user, ObjectMappRejetMotif data, String matricule, EActionDocument action,
//                                  String langCode) throws Exception {
//        Result<E> result = createResult(langCode);
//        List<E> documents = repository.findByIdIsIn(data.getIds());
//        Employe manager = employeRepository.findByUser(user);
//        for (E doc : documents) {
//            Result<Exercice> exoRes = exerciceService.checkExercice(doc.getExercice(), DEFAULT_LANG);
//            if (doc.getEtat() != EEtatDocument.ENCOURS) {
//                result = super.getInvalidResult(null, langCode);
//                result.setMessage("Aucune demande de validation en cours pour ce document :" + doc.getCode());
//                return result;
//            }
////			if (isNeedDerogation(doc, action)) {
////				if (data.getMotif()!=null) {
////					doc.setDernierMotifRejet(data.getMotif());
////					update(user, doc, DEFAULT_LANG).getEntity();
////				}
////				return sendToDerogation(user, doc, null, EActionDerogation.REJET_VALIDATION, langCode);
////			}
//            if (!exoRes.isValid()) {
//                result = super.getInvalidResult(new ArrayList<>(exoRes.getValidators()), langCode);
//                return result;
//            } else {
//                // Je recharge l'object en question
//                doc = (E) repository.findOne(doc.getId());
//                // Rechercher l'ancienne transmisson et la marqué comme traité
//                List<Transmission> oldTransmissions = transmissionRepository
//                        .findByDocument_CodeAndDestinataires_MatriculeAndTraite(doc.getCode(), matricule, false);
//                for (Transmission transmission : oldTransmissions) {
//                    transmission.setTraite(true);
//                    transmission.setDateReponse(new Date());
//                    transmission = transmissionRepository.saveAndFlush(transmission);
//                }
//                doc.setEtat(EEtatDocument.REJETER);
//                doc.setModifiable(true);
//                doc.setDateDernierRejet(new Date());
//                doc.setDernierMotifRejet(data.getMotif());
//                doc.setDernierRejetteur((manager.getNoms() != null ? manager.getNoms() : "")
//                        + (manager.getPrenom() != null ? manager.getPrenom() : ""));
//
//
//            }
//        }
//
//        // // Je crée un évènement
//        // postEvent(user, document.getCode(), action, matricule,
//        // matDestinataire);
//
//        String message = "Document rejeté";
//        result.setType(EReturnType.Entity);
//        result.setValid(true);
//        result.setMessage(message);
//        return result;
//    }
//}
