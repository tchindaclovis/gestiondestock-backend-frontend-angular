import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {
  CommandeclientfournisseurService
} from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
import {CommandeClientDto, LigneCommandeClientDto} from "../../../gs-api/src";
import {Observable} from "rxjs";

@Component({
  selector: 'app-page-commande-client-fournisseur',
  templateUrl: './page-commande-client-fournisseur.component.html',
  styleUrls: ['./page-commande-client-fournisseur.component.scss']
})
export class PageCommandeClientFournisseurComponent implements OnInit {

  @Input() origin = '';
  listeCommandes: Array<any> = [];
  // Initialisation de la Map pour éviter les erreurs de lecture immédiate dans le template
  mapLignesCommande = new Map<number, any[]>();
  mapPrixTotalCommande = new Map<number, number>();

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data =>{
      this.origin = data['origin'];
      this.findAllCommandes(); // Déplacé ici pour garantir que l'origin est chargé
    });
  }

  findAllCommandes(): void {
    let serviceCall: Observable<any>;

      serviceCall = (this.origin === 'client') ?
        this.commandeClientFournisseurService.findAllCommandesClient() :
        this.commandeClientFournisseurService.findAllCommandesFournisseur();


    serviceCall.subscribe(cmd => {
      this.listeCommandes = cmd;
      // Initialiser la Map avec des tableaux vides pour CHAQUE commande immédiatement
      this.listeCommandes.forEach(c => {
        this.mapLignesCommande.set(c.id, []);
        this.mapPrixTotalCommande.set(c.id, 0);
      });
      this.findAllLigneCommande();
    });
  }

  findAllLigneCommande(): void {
    this.listeCommandes.forEach(cmd => {
      this.findLignesCommande(cmd.id);
    });
  }

  findLignesCommande(idCommande: number): void {
    // 1. Déclaration avec initialisation pour éviter l'erreur "used before being assigned"
    let serviceLignes: Observable<any>;

      serviceLignes = (this.origin === 'client') ?
        this.commandeClientFournisseurService.findAllLigneCommandesClient(idCommande) :
        this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(idCommande);


    serviceLignes.subscribe(list => {
      // Si le retour est un Blob (fréquent dans votre projet), on le gère
      if (list instanceof Blob) {
        list.text().then(text => {
          const parsedList = JSON.parse(text);
          this.mapLignesCommande.set(idCommande, parsedList);
          this.mapPrixTotalCommande.set(idCommande, this.calcTotalCmd(parsedList));
        });
      } else {
        this.mapLignesCommande.set(idCommande, list || []);
        this.mapPrixTotalCommande.set(idCommande, this.calcTotalCmd(list || []));
      }
    });
  }

  calcTotalCmd(list: Array<any>): number {
    let total = 0;
    if (list) {
      list.forEach(ligne => {

        const pu = (this.origin === 'client') ?
          ligne.prixVenteUnitaireTtc :
          ligne.prixUnitaireTtc;

        total += (+pu * +ligne.quantite);

        // // Utilisation du prix unitaire ou prix de vente selon l'objet
        // const pu = ligne.prixUnitaire  || ligne.prixVenteUnitaireTtc|| ligne.prixUnitaireTtc  || 0;
        // total += (+pu * +ligne.quantite);
      });
    }
    return Math.floor(total);
  }

  calculerTotalCommande(id: number): number {
    return this.mapPrixTotalCommande.get(id) || 0;
  }

  nouvelleCommande(): void {

    const route = (this.origin === 'client') ?
      'nouvellecommandeclient' :
      'nouvellecommandefournisseur';

    this.router.navigate([route]);
  }

  handleSuppression(result: string): void {
    if (result === 'success') {
      // On appelle simplement la méthode qui charge vos ventes
      // Cela mettra à jour la liste 'ventes' et Angular rafraîchira l'écran instantanément
      this.findAllCommandesClientFournisseur();
    } else {
      // // Optionnel : afficher un message d'erreur si le signal n'est pas 'success'
      // this.errorMsg = result;
    }
  }

  findAllCommandesClientFournisseur(): void {
    if (this.origin === 'client') {
      this.commandeClientFournisseurService.findAllCommandesClient()
        .subscribe(res => {
          this.listeCommandes = res;
        });
    }else {
      this.commandeClientFournisseurService.findAllCommandesFournisseur()
        .subscribe(res => {
          this.listeCommandes = res;
        });
    }
  }
}









// import { Component, OnInit } from '@angular/core';
// import {ActivatedRoute, Router} from "@angular/router";
// import {
//   CommandeclientfournisseurService
// } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
// import {CommandeClientDto, LigneCommandeClientDto} from "../../../gs-api/src";
//
// @Component({
//   selector: 'app-page-commande-client-fournisseur',
//   templateUrl: './page-commande-client-fournisseur.component.html',
//   styleUrls: ['./page-commande-client-fournisseur.component.scss']
// })
// export class PageCommandeClientFournisseurComponent implements OnInit {
//
//   origin = '';
//   listeCommandes: Array<any> = [];
//   lignesCommande: Array<any> = [];
//   // lignesCommande: Array<LigneCommandeClientDto> = [];
//
//   // lignesCommandeParCommande: { [key: number]: LigneCommandeClientDto[] } = {};
//   mapLignesCommande = new Map();
//   mapPrixTotalCommande = new Map();
//
//
//
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private commandeClientFournisseurService: CommandeclientfournisseurService
//   ) { }
//
//   ngOnInit(): void {
//     this.activatedRoute.data.subscribe(data =>{
//       this.origin = data['origin'];
//     });
//     this.findAllCommandes();
//   }
//
//   findAllCommandes(): void {
//     if(this.origin === 'client') {
//       this.commandeClientFournisseurService.findAllCommandesClient()
//         .subscribe(cmd => {
//           this.listeCommandes = cmd;
//           this.findAllLigneCommande();
//         });
//
//     }else  if(this.origin === 'fournisseur'){
//       this.commandeClientFournisseurService.findAllCommandesFournisseur()
//         .subscribe(cmd => {
//           this.listeCommandes = cmd;
//           this.findAllLigneCommande();
//         });
//     }
//   }
//
//
//   findAllLigneCommande(): void {
//     this.listeCommandes.forEach(cmd =>{
//       this.findLignesCommande(cmd.id)
//     });
//   }
//
//
//   nouvelleCommande(): void{
//     if(this.origin === 'client'){
//       this.router.navigate(['nouvellecommandeclient']);
//     }else  if(this.origin === 'fournisseur'){
//       this.router.navigate(['nouvellecommandefournisseur']);
//     }
//   }
//
//
//   findLignesCommande(idCommande?: number): void {
//     if(this.origin === 'client'){
//       this.commandeClientFournisseurService.findAllLigneCommandesClient(idCommande)
//         .subscribe(list => {
//           this.mapLignesCommande.set(idCommande, list); //la clé est l'idCommande et la valeur est la liste des lignes commandes
//           this.mapPrixTotalCommande.set(idCommande, this.calcTotalCmd(list));
//         });
//     } else if(this.origin === 'fournisseur'){
//       this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(idCommande)
//         .subscribe(list => {
//           this.mapLignesCommande.set(idCommande, list); //la clé est l'idCommande et la valeur est la liste des lignes commandes
//           this.mapPrixTotalCommande.set(idCommande, this.calcTotalCmd(list));
//         });
//     }
//   }
//
//
//   calcTotalCmd(list: Array<any>): number {
//     let total = 0;
//     list.forEach(ligne => {
//       if (ligne.prixUnitaire && ligne.quantite) {
//         total += +ligne.prixUnitaire * +ligne.quantite;
//       }
//     });
//     return Math.floor(total);
//   }
//
//   calculerTotalCommande(id?: number): number {
//     return this.mapPrixTotalCommande.get(id);
//   }
// }
