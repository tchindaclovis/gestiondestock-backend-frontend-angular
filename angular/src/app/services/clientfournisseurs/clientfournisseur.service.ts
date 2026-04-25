import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {ArticleDto, ClientDto, ClientsService, FournisseurDto, FournisseursService} from "../../../gs-api/src";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ClientfournisseurService {

  constructor(
    private userService: UserService,
    private clientsService: ClientsService,
    private fournisseursService: FournisseursService
  ) { }

  enregistrerClient(clientDto: ClientDto): Observable<ClientDto>{
    clientDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id
    return this.clientsService.save6(clientDto);
  }

  enregistrerFournisseur(fournisseurDto: FournisseurDto): Observable<FournisseurDto>{
    fournisseurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id
    return this.fournisseursService.save2(fournisseurDto);
  }

  findAllClients(): Observable<ClientDto[]>{
    return this.clientsService.findAll6();
  }

  findAllFournisseurs(): Observable<FournisseurDto[]>{
    return this.fournisseursService.findAll2();
  }

  findAllClientByIdEntreprise(idEntreprise: number): Observable<ClientDto[]> {
    if (idEntreprise) {
      return this.clientsService.findAllClientByIdEntreprise(idEntreprise);
    }
    return of([]);
  }

  findAllFournisseurByIdEntreprise(idEntreprise: number): Observable<FournisseurDto[]> {
    if (idEntreprise) {
      return this.fournisseursService.findAllFournisseurByIdEntreprise(idEntreprise);
    }
    return of([]);
  }

  findClientById(id: number): Observable<ClientDto>{
    if(id){
      return this.clientsService.findById6(id);
    }
    return of();
  }

  findFournisseurById(id: number): Observable<FournisseurDto>{
    if(id){
      return this.fournisseursService.findById2(id);
    }
    return of();
  }

  deleteClient(idClient: number): Observable<any>{
    if(idClient){
      return this.clientsService.delete6(idClient);
    }
    return of();
  }

  deleteFournisseur(idFournisseur: number): Observable<any>{
    if(idFournisseur){
      return this.fournisseursService.delete2(idFournisseur);
    }
    return of();
  }
}
