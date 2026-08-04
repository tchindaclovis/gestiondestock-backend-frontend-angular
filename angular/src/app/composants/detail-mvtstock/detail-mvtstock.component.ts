import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { VenteService } from 'src/app/services/vente/vente.service';
import {MvtStockDto} from "../../../gs-api/src";
import { VenteDto } from "../../../gs-api/src";

@Component({
  selector: 'app-detail-mvtstock',
  templateUrl: './detail-mvtstock.component.html',
  styleUrls: ['./detail-mvtstock.component.scss']
})
export class DetailMvtstockComponent implements OnInit, OnChanges {

  @Input() origin = '';

  @Input() mvtStockDto: MvtStockDto = {};

  @Input() ligneCommande: any = {};


  // 💡 Vente récupérée depuis le backend si le mouvement est lié à une vente
  venteDto: VenteDto | null = null;

  constructor(
    private activatedRoute: ActivatedRoute,
    private venteService: VenteService
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['origin'];
    });

    console.log('Données reçues dans le détail :', this.mvtStockDto);
    this.chargerVenteSiLiee();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ligneCommande']) {
      console.log('Ligne Commande reçue :', this.ligneCommande);
    }
    if (changes['mvtStockDto']) {
      console.log('Mouvement Stock reçu :', this.mvtStockDto);
      // Réattache la vente si le DTO de mouvement change
      this.chargerVenteSiLiee();
    }
  }

  /**
   * 🔍 Charge la vente depuis le backend si le mouvement provient d'une vente (codeSource "CVT...")
   */
  private chargerVenteSiLiee(): void {
    const codeSource = this.mvtStockDto?.codeSource;

    // On vérifie si la source est une Vente (commence par 'CVT')
    if (codeSource && codeSource.toUpperCase().startsWith('CVT')) {
      // Appel du service pour charger la vente par son code
      this.venteService.findVenteByCode(codeSource).subscribe({
        next: (vente: VenteDto) => {
          this.venteDto = vente;
        },
        error: (err) => {
          console.warn('Impossible de charger la vente pour le codeSource :', codeSource, err);
          this.venteDto = null;
        }
      });
    } else {
      this.venteDto = null;
    }
  }

  /**
   * 🛡️ RÈGLE MÉTIER :
   * Indique si le mouvement provient d'une vente elle-même liée à une commande client
   * (c.-à-d. codeCommandeClient existe et n'est pas vide).
   */
  get estVenteIssueDeCommande(): boolean {
    return !!(
      this.venteDto &&
      this.venteDto.codeCommandeClient &&
      this.venteDto.codeCommandeClient.trim() !== ''
    );
  }

  // Détermine si le mouvement est une Entrée ou une Sortie
  get isEntree(): boolean {
    return this.mvtStockDto.typeMvt === 'ENTREE' ||
      this.mvtStockDto.typeMvt === 'CORRECTION_POS' ||
      this.mvtStockDto.typeMvt === 'CORRECTION_POS_VENTE_RED';
  }

  // Retourne un libellé propre selon le type enum
  get typeMvtLabel(): string {
    switch (this.mvtStockDto.typeMvt) {
      case 'ENTREE': return 'AchatAuFournisseurConfirm';
      case 'SORTIE': return 'CommandeClientConfirm';
      case 'SORTIE_VTE': return 'VenteAuClient';
      case 'SORTIE_VTE_CMD': return 'VenteAuClientCmd';
      case 'CORRECTION_POS': return 'Correction(+)Erreur';
      case 'CORRECTION_NEG': return 'Correction(-)Perte';
      case 'CORRECTION_POS_VENTE_RED': return 'Correction(+)RetourArticle';
      case 'CORRECTION_NEG_VENTE_AUG': return 'Correction(-)VenteAuClientAug';
      case 'CORRECTION_NEG_RETOUR_FOURNISSEUR': return 'Correction(-)RetourAuFournissr';
      default: return this.mvtStockDto.typeMvt || 'INCONNU';
    }
  }

  get sourceMvtLabel(): string {
    switch (this.mvtStockDto.sourceMvt) {
      case 'COMMANDE_CLIENT': return 'Commande du client';
      case 'COMMANDE_FOURNISSEUR': return 'Commande au fournisseur';
      case 'VENTE': return 'Vente au client';
      case 'VENTE_CMD': return 'Vente au client cmd';
      case 'CORRECTION_STOCK': return 'Correction de stock';
      default: return this.mvtStockDto.sourceMvt || 'INCONNU';
    }
  }


  calculerTotalMvtStock(): number {
    if (!this.mvtStockDto || !this.mvtStockDto.quantite) {
      return 0;
    }

    let prix = 0;

    switch (this.mvtStockDto.typeMvt) {
      case 'ENTREE':
      case 'CORRECTION_POS':
        prix = -(this.mvtStockDto.article?.prixUnitaireTtc || 0);
        break;

      case 'CORRECTION_NEG_RETOUR_FOURNISSEUR':
        prix = this.mvtStockDto.article?.prixUnitaireTtc || 0;
        break;

      case 'CORRECTION_POS_VENTE_RED':
        prix = -(this.mvtStockDto.article?.prixVenteUnitaireTtc || 0);
        break;

      case 'SORTIE_VTE':
      case 'SORTIE_VTE_CMD':
      case 'CORRECTION_NEG_VENTE_AUG':
        prix = -(this.mvtStockDto.article?.prixVenteUnitaireTtc || 0);
        break;

      case 'SORTIE':
      case 'CORRECTION_NEG':
      default:
        prix = 0;
        break;
    }

    return +prix * +this.mvtStockDto.quantite;
  }
}






// import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
// import { ActivatedRoute } from '@angular/router';
// import { VenteService } from 'src/app/services/vente/vente.service';
// import {MvtStockDto} from "../../../gs-api/src";
//
// @Component({
//   selector: 'app-detail-mvtstock',
//   templateUrl: './detail-mvtstock.component.html',
//   styleUrls: ['./detail-mvtstock.component.scss']
// })
// export class DetailMvtstockComponent implements OnInit {
//
//   @Input() origin = ''; // On peut aussi le passer par @Input() depuis le parent pour plus de rapidité
//
//   @Input() mvtStockDto: MvtStockDto = {}; // Utilisation directe du DTO de mouvement
//
//   @Input() ligneCommande: any = {};
//
//   constructor(
//     private activatedRoute: ActivatedRoute,
//     private venteService: VenteService
//   ) {}
//
//   ngOnInit(): void {
//     // On garde ceci par sécurité, mais l'Input prendra le dessus
//     this.activatedRoute.data.subscribe(data => {
//       this.origin = data['origin'];
//     });
//
//     console.log('Données reçues dans le détail :', this.mvtStockDto);
//   }
//
//   // 🛡️ AJOUTEZ CECI POUR CONTROLER LES ENTRÉES
//   ngOnChanges(changes: SimpleChanges): void {
//     if (changes['ligneCommande']) {
//       console.log('Ligne Commande reçue :', this.ligneCommande);
//     }
//     if (changes['mvtStockDto']) {
//       console.log('Mouvement Stock reçu :', this.mvtStockDto);
//     }
//   }
//
//   // Détermine si le mouvement est une Entrée ou une Sortie
//   get isEntree(): boolean {
//     return this.mvtStockDto.typeMvt === 'ENTREE' ||
//            this.mvtStockDto.typeMvt === 'CORRECTION_POS' ||
//            this.mvtStockDto.typeMvt === 'CORRECTION_POS_VENTE_RED';
//   }
//
//
//   // Retourne un libellé propre selon le type enum
//   get typeMvtLabel(): string {
//     switch (this.mvtStockDto.typeMvt) {
//       case 'ENTREE': return 'AchatAuFournisseurConfirm';
//       case 'SORTIE': return 'CommandeClientConfirm';
//       case 'SORTIE_VTE': return 'VenteAuClient';
//       case 'CORRECTION_POS': return 'Correction(+)Erreur';
//       case 'CORRECTION_NEG': return 'Correction(-)Perte';
//       case 'CORRECTION_POS_VENTE_RED': return 'Correction(+)RetourArticle';
//       case 'CORRECTION_NEG_VENTE_AUG': return 'Correction(-)VenteAuClientAug';
//       case 'CORRECTION_NEG_RETOUR_FOURNISSEUR': return 'Correction(-)RetourAuFournissr';
//       default: return this.mvtStockDto.typeMvt || 'INCONNU';
//     }
//   }
//
//   /**
//    * Bonus : Automatisation de la Source (Commande, Vente, etc.)
//    */
//   get sourceMvtLabel(): string {
//     switch (this.mvtStockDto.sourceMvt) {
//       case 'COMMANDE_CLIENT': return 'Commande du client';
//       case 'COMMANDE_FOURNISSEUR': return 'Commande au fournisseur';
//       case 'VENTE': return 'Vente au client';
//       case 'CORRECTION_STOCK': return 'Correction de stock';
//       default: return this.mvtStockDto.sourceMvt || 'INCONNU';
//     }
//   }
//
//
//   calculerTotalMvtStock(): number {
//     // 1. Vérification des données indispensables
//     if (!this.mvtStockDto || !this.mvtStockDto.quantite) {
//       return 0;
//     }
//
//     let prix = 0;
//
//     // 2. Attribution du bon prix selon le type de mouvement (sans casser la fonction)
//     switch (this.mvtStockDto.typeMvt) {
//       case 'ENTREE':
//       case 'CORRECTION_POS':
//         prix = -(this.mvtStockDto.article?.prixUnitaireTtc || 0);
//         break; //le mot-clé break fait simplement sortir du bloc switch une fois le prix affecté
//
//       case 'CORRECTION_NEG_RETOUR_FOURNISSEUR':
//         prix = this.mvtStockDto.article?.prixUnitaireTtc || 0;
//         break;
//
//       case 'CORRECTION_POS_VENTE_RED':
//         prix = -(this.mvtStockDto.article?.prixVenteUnitaireTtc || 0);
//         break;
//
//       case 'SORTIE_VTE':
//       case 'CORRECTION_NEG_VENTE_AUG':
//         prix = -(this.mvtStockDto.article?.prixVenteUnitaireTtc || 0);
//         break;
//
//       case 'SORTIE': // Utile pour afficher "--" ou 0 dans le template selon vos besoins
//       case 'CORRECTION_NEG':
//       default:
//         prix = 0;
//         break;
//     }
//
//     // 3. Calcul final et renvoi du total réel (Prix * Quantité)
//     return +prix * +this.mvtStockDto.quantite;
//   }
// }


























// calculerTotalMvtStock(): number {
//   if (!this.mvtStockDto || !this.mvtStockDto.quantite || !this.mvtStockDto.codeSource) {
//     return 0;
//   }
//
//   let prix = 0;
//
//   const prefixeCode = this.mvtStockDto.codeSource.substring(0, 3).toUpperCase();
//
//   // 2. Affectation de la valeur selon le contexte
//   if (prefixeCode === 'CVT') {
//     prix = this.mvtStockDto.article?.prixVenteUnitaireTtc || 0;
//   } else if (prefixeCode === 'CMF') {
//     prix = this.mvtStockDto.article?.prixUnitaireTtc || 0;
//   }
//
//   return +prix * +this.mvtStockDto.quantite;
// }




// // On crée un dictionnaire qui lie les valeurs de l'Enum à leur libellé
//   const TYPE_MVT_LABELS: Record<MvtStockDto.TypeMvtEnum, string> = {
//     [MvtStockDto.TypeMvtEnum.Entree]: 'ACHAT FOURNISSEUR',
//     [MvtStockDto.TypeMvtEnum.Sortie]: 'VENTE CLIENT',
//     [MvtStockDto.TypeMvtEnum.CorrectionPos]: 'CORRECTION (+)',
//     [MvtStockDto.TypeMvtEnum.CorrectionNeg]: 'CORRECTION (-)',
//     [MvtStockDto.TypeMvtEnum.CorrectionPosVenteRed]: 'CORRECTION (+) VENTE',
//     [MvtStockDto.TypeMvtEnum.CorrectionNegVenteAug]: 'CORRECTION (-) VENTE'
//   };
//
//
//   get typeMvtLabel(): string {
//     const type = this.mvtStockDto?.typeMvt;
//
//     // Si le type existe et est présent dans notre mapper, on renvoie le libellé
//     // Sinon on renvoie la valeur brute ou 'INCONNU'
//     return type ? ((this.TYPE_MVT_LABELS)[type] || type) : 'INCONNU';
//   }




// import { Component, Input, OnInit } from '@angular/core';
// import { MvtStockDto } from "../../../gs-api/src";
// import {UserService} from "../../services/user/user.service";
// import {MvtstockService} from "../../services/mvtstock/mvtstock.service";
// import {ArticleService} from "../../services/article/article.service";
//
// /**
//  * Configuration des libellés et des sens de mouvement (Entrée/Sortie)
//  * On utilise le Namespace généré par OpenAPI pour rester cohérent avec le Backend.
//  */
// const MVT_CONFIG: Record<MvtStockDto.TypeMvtEnum, { label: string, isEntree: boolean }> = {
//   [MvtStockDto.TypeMvtEnum.Entree]: { label: 'ACHAT FOURNISSEUR', isEntree: true },
//   [MvtStockDto.TypeMvtEnum.Sortie]: { label: 'VENTE CLIENT', isEntree: false },
//   [MvtStockDto.TypeMvtEnum.CorrectionPos]: { label: 'CORRECTION(+)', isEntree: true },
//   [MvtStockDto.TypeMvtEnum.CorrectionNeg]: { label: 'CORRECTION(-)', isEntree: false },
//   [MvtStockDto.TypeMvtEnum.CorrectionPosVenteRed]: { label: 'CORRECTION(+)VENTE', isEntree: true },
//   [MvtStockDto.TypeMvtEnum.CorrectionNegVenteAug]: { label: 'CORRECTION(-)VENTE', isEntree: false }
// };
//
// @Component({
//   selector: 'app-detail-mvtstock',
//   templateUrl: './detail-mvtstock.component.html',
//   styleUrls: ['./detail-mvtstock.component.scss']
// })
// export class DetailMvtstockComponent implements OnInit {
//
//   @Input() mvtStockDto: MvtStockDto = {};
//
//   constructor(
//     private userService: UserService,
//     private mvtStockService: MvtstockService,
//     private articleService: ArticleService
//   ) {}
//
//   ngOnInit(): void {}
//
//   /**
//    * Automatisation du sens du mouvement
//    * Retourne true pour les entrées/corrections positives, false pour les sorties/négatives
//    */
//   get isEntree(): boolean {
//     const type = this.mvtStockDto.typeMvt;
//     return type ? MVT_CONFIG[type]?.isEntree : false;
//   }
//
//   /**
//    * Automatisation du libellé
//    * Plus de switch : on pioche directement dans la configuration
//    */
//   get typeMvtLabel(): string {
//     const type = this.mvtStockDto.typeMvt;
//     return type ? (MVT_CONFIG[type]?.label || type) : 'INCONNU';
//   }
//
//   /**
//    * Bonus : Automatisation de la Source (Commande, Vente, etc.)
//    */
//   get sourceMvtLabel(): string {
//     const source = this.mvtStockDto.sourceMvt;
//     if (!source) return 'N/A';
//     // On peut transformer 'COMMANDE_CLIENT' en 'Commande Client' proprement
//     return source.replace(/_/g, ' ');
//   }
// }




