import { Component, EventEmitter, Input, OnInit, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from "@angular/router";
import { ClientfournisseurService } from "../../services/clientfournisseurs/clientfournisseur.service";
import { CommandeclientfournisseurService } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";

@Component({
  selector: 'app-detail-commande-client-fournisseur',
  templateUrl: './detail-commande-client-fournisseur.component.html',
  styleUrls: ['./detail-commande-client-fournisseur.component.scss']
})
export class DetailCommandeClientFournisseurComponent implements OnInit, OnChanges {

  @Input() origin = '';

  @Input() commande: any = {};

  @Input() clientFournisseur: any;

  listeCommandes: any = {};

  @Output() suppressionTerminee = new EventEmitter<number>();
  @Output() suppressionResult = new EventEmitter<string>();

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }

  ngOnInit(): void {
    // On s'abonne aux data de la route pour connaître l'origine (client ou fournisseur)
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['origin'];
      this.extractClientFournisseur();
    });
  }

  // Crucial : Détecter quand l'objet 'commande' arrive du composant parent
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['commande']) {
      this.extractClientFournisseur();
    }
  }

  extractClientFournisseur(): void {
    if (this.commande) {
      this.clientFournisseur = (this.origin === 'client')
        ? this.commande.client
        : this.commande.fournisseur;
    }
  }

  modifierCommandeClientFournisseur(): void {
    const route = this.origin === 'client' ? 'nouvellecommandeclient' : 'nouvellecommandefournisseur';
    // On navigue vers la page de modification avec l'ID de la commande
    this.router.navigate([route, this.commande.id]);
  }


  // confirmerEtSupprimer(): void {
  //   if (this.commande && this.commande.id) {
  //     const delete$ = this.origin === 'client'
  //       ? this.commandeClientFournisseurService.deleteCommandeClient(this.commande.id)
  //       : this.commandeClientFournisseurService.deleteCommandeFournisseur(this.commande.id);
  //
  //     delete$.subscribe({
  //       next: () => {
  //         // 1. On informe le parent AVANT toute navigation
  //         this.suppressionTerminee.emit(this.commande.id);
  //         this.suppressionResult.emit('success');
  //
  //         // Note : On ne force plus la navigation ici car l'élément
  //         // aura déjà disparu visuellement de la liste.
  //       },
  //       error: (err) => {
  //         console.error("Erreur suppression:", err);
  //         this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression');
  //       }
  //     });
  //   }
  // }


  confirmerEtSupprimer(): void {
    if (this.origin === 'client') {
      // ATTENTION : On supprime la COMMANDE (commande.id), pas le client !
      if (this.commande && this.commande.id) {
        this.commandeClientFournisseurService.deleteCommandeClient(this.commande.id)
          .subscribe({
            next: () => {
              // 1. On émet d'abord le succès pour les éventuels composants parents
              this.suppressionResult.emit('success');
              // this.listeCommandes = this.listeCommandes.filter(cmd => cmd.id !== this.commande.id);
              // 2. On attend la fermeture de la modal (300ms) avant de naviguer
              setTimeout(() => {
                // 3. Navigation vers la liste des ventes
                // Cela déclenchera le rechargement du composant de la liste
                this.router.navigate(['commandesclient']);
              }, 300);
            },
            error: (err) => {
              console.error("Erreur suppression:", err);
              this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression');
            }
          });
      }
    } else {
      if (this.commande && this.commande.id) {
        this.commandeClientFournisseurService.deleteCommandeFournisseur(this.commande.id)
          .subscribe({
            next: () => {
              // 1. On émet d'abord le succès pour les éventuels composants parents
              this.suppressionResult.emit('success');
              // 2. On attend la fermeture de la modal (300ms) avant de naviguer
              setTimeout(() => {
                // 3. Navigation vers la liste des ventes
                // Cela déclenchera le rechargement du composant de la liste
                this.router.navigate(['commandesfournisseur']);
              }, 300);
            },
            error: (err) => {
              console.error("Erreur suppression:", err);
              this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression');
            }
          });
      }
    }
  }


  // confirmerEtSupprimer(): void {
  //   if (this.origin === 'client') {
  //     // ATTENTION : On supprime la COMMANDE (commande.id), pas le client !
  //     this.commandeClientFournisseurService.deleteCommandeClient(this.commande.id)
  //       .subscribe({
  //         next: () => this.suppressionResult.emit('success'),
  //         error: (err) => this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression')
  //       });
  //   } else {
  //     this.commandeClientFournisseurService.deleteCommandeFournisseur(this.commande.id)
  //       .subscribe({
  //         next: () => this.suppressionResult.emit('success'),
  //         error: (err) => this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression')
  //       });
  //   }
  // }
}







// import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
// import {ClientDto, CommandeClientDto, LigneCommandeClientDto} from "../../../gs-api/src";
// import {
//   CommandeclientfournisseurService
// } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
// import {ActivatedRoute, Router} from "@angular/router";
// import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";
// import {ArticleService} from "../../services/article/article.service";
//
// @Component({
//   selector: 'app-detail-commande-client-fournisseur',
//   templateUrl: './detail-commande-client-fournisseur.component.html',
//   styleUrls: ['./detail-commande-client-fournisseur.component.scss']
// })
// export class DetailCommandeClientFournisseurComponent implements OnInit {
//
//   @Input()
//   origin = '';
//   @Input()
//   commande: any = {};
//
//   clientFournisseur: any = {}; //soit client soit fournisseur
//
//   @Output()
//   suppressionResult = new EventEmitter();
//
//   listClientsFournisseurs: Array<any> = [];
//
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private clientFournisseurService:ClientfournisseurService,
//     private commandeClientFournisseurService:CommandeclientfournisseurService
//   ) { }
//
//
//   ngOnInit(): void {
//     this.extractClientFournisseur();
//     this.activatedRoute.data.subscribe(data =>{
//       this.origin = data['origin'];
//     });
//     this.findAllClientsFournisseurs();
//
//   }
//
//
//   findAllClientsFournisseurs(): void {
//     if (this.origin === 'client') {
//       this.clientFournisseurService.findAllClients()
//         .subscribe(clients => {
//           this.listClientsFournisseurs = clients;
//         });
//     } else if (this.origin === 'fournisseur' ) {
//       this.clientFournisseurService.findAllFournisseurs()
//         .subscribe(fournisseurs => {
//           this.listClientsFournisseurs = fournisseurs;
//         });
//     }
//   }
//
//
//   extractClientFournisseur(): void {
//     if(this.origin === 'client'){
//       this.clientFournisseur = this.commande?.client;
//     } else if(this.origin === 'fournisseur'){
//       this.clientFournisseur = this.commande?.fournisseur;
//     }
//   }
//
//   modifierCommandeClientFournisseur() {
//     if(this.origin === 'client'){
//       this.router.navigate(['nouvellecommandeclient', this.clientFournisseur.id]);
//     } else if(this.origin === 'fournisseur'){
//       this.router.navigate(['nouvellecommandefournisseur', this.clientFournisseur.id]);
//     }
//   }
//
//   confirmerEtSupprimer(): void {
//     if(this.origin === 'client'){
//       this.commandeClientFournisseurService.deleteCommandeClient(this.clientFournisseur.id)
//         .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
//           this.suppressionResult.emit('success');
//         }, error => {
//           this.suppressionResult.emit(error.error.error);
//         });
//
//     } else if(this.origin === 'fournisseur'){
//       this.commandeClientFournisseurService.deleteCommandeFournisseur(this.clientFournisseur.id)
//         .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
//           this.suppressionResult.emit('success');
//         }, error => {
//           this.suppressionResult.emit(error.error.error);
//         });
//     }
//   }
// }
