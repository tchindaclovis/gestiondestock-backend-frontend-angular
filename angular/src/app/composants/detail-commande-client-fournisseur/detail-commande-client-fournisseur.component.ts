import { Component, EventEmitter, Input, OnInit, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from "@angular/router";
import { ClientfournisseurService } from "../../services/clientfournisseurs/clientfournisseur.service";
import { CommandeclientfournisseurService } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
import {CommandeClientDto, CommandeFournisseurDto} from "../../../gs-api/src";

@Component({
  selector: 'app-detail-commande-client-fournisseur',
  templateUrl: './detail-commande-client-fournisseur.component.html',
  styleUrls: ['./detail-commande-client-fournisseur.component.scss']
})
export class DetailCommandeClientFournisseurComponent implements OnInit, OnChanges {

  @Input() origin = '';

  @Input() commande: any = {};

  clientFournisseur: any;

  commandeClientDto: CommandeFournisseurDto = {}; //objet ou variable initialisé à vide
  commandeFournisseurDto: CommandeClientDto = {}; //objet ou variable initialisé à vide

  showModalError = false;
  messageErreur = '';


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
    // 1. On utilise directement l'objet 'commande' reçu en @Input
    if (!this.commande || !this.commande.etatCommande) {
      console.error("Données de commande manquantes");
      return;
    }

    const etatActuel = this.commande.etatCommande;

    // 2. Vérification de l'état
    if (etatActuel === 'PRO_FORMAT') {
      const route = (this.origin === 'client')
        ? 'nouvellecommandeclient'
        : 'nouvellecommandefournisseur';

      this.router.navigate([route, this.commande.id]);
    } else {
      // Gestion de l'erreur via le modal
      this.messageErreur = "Cette commande ne peut être modifiée car elle est déjà à état CONFIRMEE.";
      this.showModalError = true;

      //     // 3. Affichage de l'alerte (Pop-up)
      //     // Vous pouvez utiliser window.alert ou un service de notification (Toastr, SweetAlert)
      //     // alert("Cette commande ne peut être modifiée car elle n'est plus au état PRO_FORMAT.");
    }
  }


  // modifierCommandeClientFournisseur(): void {
  //   const route = (this.origin === 'client') ? 'nouvellecommandeclient' : 'nouvellecommandefournisseur';
  //   // On navigue vers la page de modification avec l'ID de la commande
  //   this.router.navigate([route, this.commande.id]);
  // }


  confirmationClientFournisseur(): void {
    // Gestion de l'erreur via le modal
    this.messageErreur = "Etat déjà Confirmé.";
    this.showModalError = true;
  }

  confirmer(): void {
      if (this.commande && this.commande.id) {
        const id = this.commande.id;
        const nouvelEtat = 'CONFIRMEE';
        let dateConfirmation = new Date().toISOString();

        if (this.origin === 'client') {
          this.commandeClientFournisseurService.updateEtatCommandeClient(id, nouvelEtat, dateConfirmation)
            .subscribe({
              next: (res) => {
                this.commande = res; // Mettre à jour l'objet local avec le retour du serveur
                this.suppressionResult.emit('success');
                // Optionnel : notifier l'utilisateur
                console.log('État commande client mis à jour');
                // enregistrerCommande()
              },
              error: (err) => this.handleError(err)
            });

        } else {
          this.commandeClientFournisseurService.updateEtatCommandeFournisseur(id, nouvelEtat, dateConfirmation)
            .subscribe({
              next: (res) => {
                this.commande = res; // Mettre à jour l'objet local avec le retour du serveur
                this.suppressionResult.emit('success');
                // Optionnel : notifier l'utilisateur
                console.log('État commande fournisseur mis à jour');
                // enregistrerCommande()
              },
              error: (err) => this.handleError(err)
            });
        }
      }
  }




  private handleError(error: any): void {
    console.error('Erreur lors de la confirmation', error);
    this.suppressionResult.emit(error.error?.message || 'Erreur lors de la confirmation');
  }


  vendre(): void{

  }


  suppressionClientFournisseur(): void {
    this.messageErreur = "Etat déjà Confirmé.";
    this.showModalError = true;
  }

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
    } else if(this.origin === 'fournisseur') {
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

}



// enregistrerCommande(): void {
//   const commande = this.preparerCommande();
//   if (this.origin === 'client') {
//     this.commandeClientFournisseurService.enregistrerCommandeClient(commande).subscribe({
//       next:() =>{
//         this.router.navigate(['commandesclient']);
//       },
//       error: (e) => this.handleError(e)
//     });
//   } else if (this.origin === 'fournisseur') {
//     this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande).subscribe({
//       next: () => {
//         this.router.navigate(['commandesfournisseur'])
//       },
//       error: (e) => this.handleError(e)
//     });
//   }
// }
//
// private preparerCommande(): any {
//   const idEnt = this.connectedUser?.entreprise?.id;
//   // Utiliser l'ID général récupéré lors du ngOnInit (idCommande)
//   const currentId = (this.origin === 'client') ? this.idCommandeClient : this.idCommandeFournisseur;
//
//   // On détermine l'état :
//   // Si c'est une nouvelle commande (id null), on met PRO_FORMAT.
//   // Si c'est une modif, on garde l'état actuel (qui pourrait être déjà CONFIRMEE).
//   const etatActuel = (this.origin === 'client')
//     ? this.commandeFournisseurDto.etatCommande // Attention ici, vérifiez bien vos noms de variables (votre code mélangeait Dto client/fournisseur)
//     : this.commandeClientDto.etatCommande;
//
//   const lignesPourBackend = this.listeLignesCommande.map(ligne => {
//     return {
//       id: ligne.id || null, // CRITIQUE : Garder l'ID de la ligne existante
//       article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
//       quantite: ligne.quantite,
//       // On affecte le bon prix selon le contexte
//       prixVenteUnitaireTtc: (this.origin === 'client') ? ligne.prixVenteUnitaireTtc : undefined,
//       prixUnitaireTtc: (this.origin === 'fournisseur') ? ligne.prixUnitaireTtc : undefined,
//       idEntreprise: idEnt
//     };
//   });
//
//   return {
//     id: currentId, // Si présent, Hibernate fera un UPDATE // CRITIQUE : L'ID de la commande pour déclencher l'UPDATE au lieu du INSERT
//     [this.origin]: this.selectedClientFournisseur,
//     // client: { id: this.selectedClientFournisseur?.id },
//     code: (this.origin === 'client') ? this.codeCommandeClient : this.codeCommandeFournisseur,
//     dateCommande: new Date().toISOString(), // Utiliser ISOString pour la stabilité
//     etatCommande: currentId ? (etatActuel || 'PRO_FORMAT') : 'PRO_FORMAT',
//     idEntreprise: idEnt,
//     [this.origin === 'client' ? 'ligneCommandeClients' : 'ligneCommandeFournisseurs']: lignesPourBackend
//   };
// }

// confirmer(): void {
//   if (this.commande && this.commande.id) {
//
//     // Création d'un DTO simplifié et propre pour l'UPDATE
//     const commandeAUpdate: any = {
//       id: this.commande.id,
//       code: this.commande.code,
//       dateConfirmation: new Date().toISOString(),
//       etatCommande: 'CONFIRMEE', // Le changement d'état
//       idEntreprise: this.commande.idEntreprise,
//       client: this.commande.client,
//       fournisseur: this.commande.fournisseur,
//       // On s'assure que les lignes sont présentes ou vides,
//       // mais on ne change pas les quantités ici
//       ligneCommandeClients: this.commande.ligneCommandeClients || [],
//       ligneCommandeFournisseurs: this.commande.ligneCommandeFournisseurs || []
//     };
//
//     // // On crée une copie de la commande pour modifier l'état
//     // const commandeAUpdate = { ...this.commande };
//     // commandeAUpdate.etatCommande = 'CONFIRMEE';
//
//
//
//     if (this.origin === 'client') {
//       this.commandeClientFournisseurService.enregistrerCommandeClient(commandeAUpdate)
//         .subscribe({
//           next: (res) => {
//             this.suppressionResult.emit('success'); // On informe le parent de rafraîchir la liste
//             console.log('Commande Client confirmée avec succès');
//           },
//           error: (err) => this.handleError(err)
//         });
//     } else {
//       this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commandeAUpdate)
//         .subscribe({
//           next: (res) => {
//             this.suppressionResult.emit('success');
//             console.log('Commande Fournisseur confirmée avec succès');
//           },
//           error: (err) => this.handleError(err)
//         });
//     }
//   }
// }





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
