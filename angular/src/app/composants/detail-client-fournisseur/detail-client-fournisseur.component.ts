import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Router} from "@angular/router";
import { CommandeclientfournisseurService } from 'src/app/services/commandeclientfournisseur/commandeclientfournisseur.service';
import { UserService } from 'src/app/services/user/user.service';
// @ts-ignore
import {FournisseurDto, UtilisateurDto } from 'src/gs-api/src';
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";

@Component({
  selector: 'app-detail-client-fournisseur',
  templateUrl: './detail-client-fournisseur.component.html',
  styleUrls: ['./detail-client-fournisseur.component.scss']
})
export class DetailClientFournisseurComponent implements OnInit {

  @Input()
  origin = '';

  @Input()
  clientFournisseur: any = {}; //soit client soit fournisseur

  @Output()
  suppressionResult = new EventEmitter();

  connectedUser: UtilisateurDto | null = null;
  listFournisseur: Array<FournisseurDto> = [];
  showModalError = false;
  messageErreur = '';

  nombreCommandeFournisseur: number= 0;

  // Utilisation d'une Map pour stocker le nombre de commandeFournisseur par ID de fournisseur
  mapNombreCommandeFournisseurs = new Map<number, number>();

  selectedFouToDelete?: number = -1;  //initialisé à -1

  constructor(
    private router: Router,
    private userService: UserService,
    private clientFournisseurService: ClientfournisseurService,
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();
    this.findAllFounisseurs();
  }



  // modifierClientFournisseur(): void {
  //   if(this.origin === 'client'){
  //     this.router.navigate(['nouveauclient', this.clientFournisseur.id]);
  //   } else if(this.origin === 'fournisseur'){
  //
  //     this.router.navigate(['nouveaufournisseur', this.clientFournisseur.id]);
  //   }
  // }


  modifierClientFournisseur(): void {
    if(this.origin === 'client'){
      this.router.navigate(['nouveauclient', this.clientFournisseur.id]);
    } else if(this.origin === 'fournisseur'){

      // 1. Trouver le fournisseur à modifier dans la liste locale
      const fournisseurAModifier = this.clientFournisseur;

      if (!fournisseurAModifier) {
        this.messageErreur = "Fournisseur introuvable.";
        this.showModalError = true;
        return;
      }

      // 1. Récupérer l'ID de l'utilisateur connecté et l'ID du créateur
      const idUtilisateurConnecte = this.connectedUser?.id;

      //2. Récupérer l'ID du créateur du fournisseur
      // 💡 Correction : On pointe vers le champ de traçabilité de l'utilisateur, pas vers l'ID du fournisseur
      const idCreateurFournisseur = fournisseurAModifier?.idUtilisateur;  // Champ rempli par le backend

      // 3. Vérifier si l'utilisateur connecté est le créateur
      if (idUtilisateurConnecte && idUtilisateurConnecte === idCreateurFournisseur) {
        this.router.navigate(['nouveaufournisseur', this.clientFournisseur.id]);
      } else {
        // Si ce n'est pas le créateur, on affiche le modal
        this.messageErreur = "Seul l'utilisateur ayant créé ce fournisseur a le droit de le modifier.";
        this.showModalError = true;
      }
    }
  }


  findAllFounisseurs(): void {
    if(this.origin === 'fournisseur') {
      this.clientFournisseurService.findAllFournisseurs()
        .subscribe(res => {
          this.listFournisseur = res;
          // Une fois les catégories chargées, on calcule le nombre d'articles pour chacune
          this.calculerNombreCommandeFournisseurs();
        });
    }
  }


  calculerNombreCommandeFournisseurs(): void {
    this.listFournisseur.forEach(fou => {
      if (fou.id) {
        this.commandeClientFournisseurService.findAllCommandeFournisseurByIdFournisseur(fou.id)
          .subscribe(commandes => {
            this.mapNombreCommandeFournisseurs.set(fou.id!, commandes.length);
          });
      }
    });
  }


  getNombreCommandeFournisseurs(idFou?: number): number {
    if (idFou && this.mapNombreCommandeFournisseurs.has(idFou)) {
      return this.mapNombreCommandeFournisseurs.get(idFou)!;
    }
    return 0;
  }


  annulerSuppression(): void {
    this.clientFournisseur.id = -1;
  }


  selectFouPourSupprimer(id?: number): void {
    if (!id) {
      this.messageErreur = "Fournisseur introuvable.";
      this.showModalError = true;
      return;
    }

    // 1. Trouver le fournisseur dans la liste locale
    const fouA_Supprimer = this.listFournisseur.find(fou => fou.id === id);

    // 2. RÈGLE DE GESTION : Vérifier si le fournisseur possède des commandes
    const nbreCommandeFournisseurs = this.mapNombreCommandeFournisseurs.get(id) || 0;
    const idUtilisateurConnecte = this.connectedUser?.id;
    const idCreateurFournisseur = fouA_Supprimer?.idUtilisateur;

    if (nbreCommandeFournisseurs > 0) {
      // CORRECTION DU MESSAGE EXIGÉ
      this.messageErreur = "Ce fournisseur est déjà associée à au moins une commande.";
      this.showModalError = true;
      return;
    }

    // 3. RÈGLE DE SÉCURITÉ : Seul le créateur peut supprimer
    if (idUtilisateurConnecte && idUtilisateurConnecte === idCreateurFournisseur) {
      this.clientFournisseur.id = id;
      return;
    } else {
      this.messageErreur = "Seul l'utilisateur ayant créé ce fournisseur a le droit de la supprimer.";
      this.showModalError = true;
      return;
    }
  }


  confirmerEtSupprimer(): void {
    if (this.origin === 'client') {
      this.clientFournisseurService.deleteClient(this.clientFournisseur.id)
        .subscribe({
          next: () => this.suppressionResult.emit('success'),
          error: (err) => this.suppressionResult.emit(err.error.error)
        });
    } else if (this.origin === 'fournisseur') {
      if (this.clientFournisseur.id !== -1) {
        this.clientFournisseurService.deleteFournisseur(this.clientFournisseur.id)
          .subscribe({
            next: () => this.suppressionResult.emit('success'),
            error: (err) => this.suppressionResult.emit(err.error.error)
          });
      }
    }
  }

  // confirmerEtSupprimer(): void {
  //   if(this.origin === 'client'){
  //     this.clientFournisseurService.deleteClient(this.clientFournisseur.id)
  //       .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
  //         this.suppressionResult.emit('success');
  //       }, error => {
  //         this.suppressionResult.emit(error.error.error);
  //       });
  //
  //   } else if(this.origin === 'fournisseur'){
  //     if(this.clientFournisseur.id !== -1) {
  //       this.clientFournisseurService.deleteFournisseur(this.clientFournisseur.id)
  //         .subscribe(res => {  //subscribe parle d'une action par l'opérateur (suppression)
  //           this.suppressionResult.emit('success');
  //         }, error => {
  //           this.suppressionResult.emit(error.error.error);
  //         });
  //     }
  //   }
  // }

  // confirmerEtSupprimer(): void {
  //   if(this.selectedCatToDelete !== -1){
  //     this.categoryService.delete(this.selectedCatToDelete)
  //       .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
  //         this.findAllCategories();
  //       }, error => {
  //         this.errorMsgs = error.error.message;
  //       });
  //   }
  // }


  appercuClientFournisseur(): void {
    if(this.origin === 'client'){
      this.router.navigate(['appercuclient', this.clientFournisseur.id]);
    } else if(this.origin === 'fournisseur'){
      this.router.navigate(['appercufournisseur', this.clientFournisseur.id]);
    }
  }
}
