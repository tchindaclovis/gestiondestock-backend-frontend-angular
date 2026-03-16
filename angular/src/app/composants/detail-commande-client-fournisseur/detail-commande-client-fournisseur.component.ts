import {Component, Input, OnInit} from '@angular/core';
import {ClientDto, CommandeClientDto, LigneCommandeClientDto} from "../../../gs-api/src";
import {
  CommandeclientfournisseurService
} from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";

@Component({
  selector: 'app-detail-commande-client-fournisseur',
  templateUrl: './detail-commande-client-fournisseur.component.html',
  styleUrls: ['./detail-commande-client-fournisseur.component.scss']
})
export class DetailCommandeClientFournisseurComponent implements OnInit {

  @Input()
  origin = '';
  @Input()
  commande: any = {};

  // clientFournisseur: ClientDto | undefined = {};
  clientFournisseur: any | undefined = {};

  constructor() { }


  ngOnInit(): void {
    this.extractClientFournisseur();
  }


  modifierClick(): void{

  }

  extractClientFournisseur(): void {
    if(this.origin === 'client'){
      this.clientFournisseur = this.commande?.client;
    } else if(this.origin === 'fournisseur'){
      this.clientFournisseur = this.commande?.fournisseur;
    }
  }
}
