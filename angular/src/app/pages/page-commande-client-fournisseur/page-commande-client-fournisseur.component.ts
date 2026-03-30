import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {
  CommandeclientfournisseurService
} from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
import {CommandeClientDto, LigneCommandeClientDto} from "../../../gs-api/src";

@Component({
  selector: 'app-page-commande-client-fournisseur',
  templateUrl: './page-commande-client-fournisseur.component.html',
  styleUrls: ['./page-commande-client-fournisseur.component.scss']
})
export class PageCommandeClientFournisseurComponent implements OnInit {

  origin = '';
  listeCommandes: Array<any> = [];
  lignesCommande: Array<any> = [];
  // lignesCommande: Array<LigneCommandeClientDto> = [];

  // lignesCommandeParCommande: { [key: number]: LigneCommandeClientDto[] } = {};
  mapLignesCommande = new Map();
  mapPrixTotalCommande = new Map();



  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data =>{
      this.origin = data['origin'];
    });
    this.findAllCommandes();
  }

  findAllCommandes(): void {
    if(this.origin === 'client') {
      this.commandeClientFournisseurService.findAllCommandesClient()
        .subscribe(cmd => {
          this.listeCommandes = cmd;
          this.findAllLigneCommande();
        });

    }else  if(this.origin === 'fournisseur'){
      this.commandeClientFournisseurService.findAllCommandesFournisseur()
        .subscribe(cmd => {
          this.listeCommandes = cmd;
          this.findAllLigneCommande();
        });
    }
  }


  findAllLigneCommande(): void {
    this.listeCommandes.forEach(cmd =>{
      this.findLignesCommande(cmd.id)
    });
  }


  nouvelleCommande(): void{
    if(this.origin === 'client'){
      this.router.navigate(['nouvellecommandeclient']);
    }else  if(this.origin === 'fournisseur'){
      this.router.navigate(['nouvellecommandefournisseur']);
    }
  }


  findLignesCommande(idCommande?: number): void {
    if(this.origin === 'client'){
      this.commandeClientFournisseurService.findAllLigneCommandesClient(idCommande)
        .subscribe(list => {
          this.mapLignesCommande.set(idCommande, list); //la clé est l'idCommande et la valeur est la liste des lignes commandes
          this.mapPrixTotalCommande.set(idCommande, this.calcTotalCmd(list));
        });
    } else if(this.origin === 'fournisseur'){
      this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(idCommande)
        .subscribe(list => {
          this.mapLignesCommande.set(idCommande, list); //la clé est l'idCommande et la valeur est la liste des lignes commandes
          this.mapPrixTotalCommande.set(idCommande, this.calcTotalCmd(list));
        });
    }
  }


  calcTotalCmd(list: Array<any>): number {
    let total = 0;
    list.forEach(ligne => {
      if (ligne.prixUnitaire && ligne.quantite) {
        total += +ligne.prixUnitaire * +ligne.quantite;
      }
    });
    return Math.floor(total);
  }

  calculerTotalCommande(id?: number): number {
    return this.mapPrixTotalCommande.get(id);
  }
}
