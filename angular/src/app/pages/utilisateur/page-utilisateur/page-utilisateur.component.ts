import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {UtilisateurDto} from "../../../../gs-api/src";
import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";
import {UserService} from "../../../services/user/user.service";

@Component({
  selector: 'app-page-utilisateur',
  templateUrl: './page-utilisateur.component.html',
  styleUrls: ['./page-utilisateur.component.scss']
})
export class PageUtilisateurComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;
  listUtilisateur: Array<UtilisateurDto> = [];
  errorMsg = '';
  constructor(
    private router: Router,
    private userService: UserService,
    private utilisateurService: UtilisateurService
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();

    this.findListUtilisateur()
  }

  findListUtilisateur(): void{
    // On récupère l'id de l'entreprise de l'utilisateur connecté
    const idEntreprise = this.connectedUser?.entreprise?.id;

    if (idEntreprise) {
      this.utilisateurService.findAllUtilisateurByIdEntreprise(idEntreprise)
        .subscribe(utilisateurs => {
          this.listUtilisateur = utilisateurs;
        });
    }
  }


  nouvelUtilisateur(): void{
    this.router.navigate(['nouvelutilisateur']);
  }


  handleSuppression(event: any): void {
    if(event === 'success'){
      this.findListUtilisateur();
    } else {
      this.errorMsg = event;
    }
  }

}
