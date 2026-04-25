import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
import {
  LigneCommandeClientDto, LigneCommandeClientsService,
  LigneCommandeFournisseurDto, LigneCommandeFournisseursService
} from "../../../gs-api/src";
import {UserService} from "../user/user.service";

@Injectable({
  providedIn: 'root'
})
export class LignecmdclientfournisseurService {

  constructor(
    private ligneCommandeClientsService: LigneCommandeClientsService,
    private ligneCommandeFournisseursService: LigneCommandeFournisseursService,
  ) { }


  findAllLigneCommandeClientByIdEntreprise(idEntreprise: number): Observable<LigneCommandeClientDto[]> {
    if (idEntreprise) {
      return this.ligneCommandeClientsService.findAllLigneCommandeClientByIdEntreprise(idEntreprise);
    }
    return of([]);
  }

  findAllLigneCommandeFournisseurByIdEntreprise(idEntreprise: number): Observable<LigneCommandeFournisseurDto[]> {
    if (idEntreprise) {
      return this.ligneCommandeFournisseursService.findAllLigneCommandeFournisseurByIdEntreprise(idEntreprise);
    }
    return of([]);
  }
}
