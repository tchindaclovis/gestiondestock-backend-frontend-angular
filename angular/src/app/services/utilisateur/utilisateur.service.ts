import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {UtilisateurDto, UtilisateursService} from "../../../gs-api/src";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {

  constructor(
    private userService: UserService,
    private utilisateursService: UtilisateursService
  ) { }

  enregistrerUtilisateur(utilisateurDto: UtilisateurDto): Observable<UtilisateurDto>{
    utilisateurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.utilisateursService.save1(utilisateurDto);
  }

  findAllUtilisateur(): Observable<UtilisateurDto[]>{  //renvoit un observable de listes d'utilisateurDto
    return this.utilisateursService.findAll1();
  }


  findUtilisateurById(idUtilisateur?: number): Observable<UtilisateurDto>{  //renvoit un observable d'utilisateurDto
    if (idUtilisateur){   //à cause du poin d'interrogation on est obligé de faire cette vérification
      return this.utilisateursService.findById1(idUtilisateur);
    }
    return of();
  }

  deleteUtilisateur(idUtilisateur: number): Observable<any> { //type de retour est un observable de any
    if(idUtilisateur){
      this.utilisateursService.delete1(idUtilisateur);
    }
    return of();  //sinon retourne un observable vide
  }
}
