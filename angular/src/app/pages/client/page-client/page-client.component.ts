import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {ClientfournisseurService} from "../../../services/clientfournisseurs/clientfournisseur.service";
import {ClientDto, UtilisateurDto} from "../../../../gs-api/src";
import {UserService} from "../../../services/user/user.service";

@Component({
  selector: 'app-page-client',
  templateUrl: './page-client.component.html',
  styleUrls: ['./page-client.component.scss']
})
export class PageClientComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;
  listClient: ClientDto[] = [];
  // ou encore: listClient: Array<ClientDto>= [];
  errorMsg = '';

  constructor(
    private router: Router,
    private userService: UserService,
    private clientFournisseurService: ClientfournisseurService
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();
    this.findAllClients()
  }

  findAllClients(): void {
    // On récupère l'id de l'entreprise de l'utilisateur connecté
    const idEntreprise = this.connectedUser?.entreprise?.id;

    if (idEntreprise) {
      this.clientFournisseurService.findAllClientByIdEntreprise(idEntreprise)
        .subscribe(clients => {
          this.listClient = clients;
        });
    }
  }

  nouveauClient(): void{
    this.router.navigate(['nouveauclient'])
  }

  handleSuppression(event: any): void {
    if(event === 'success'){
      this.findAllClients();
    } else {
      this.errorMsg = event;
    }
  }
}
