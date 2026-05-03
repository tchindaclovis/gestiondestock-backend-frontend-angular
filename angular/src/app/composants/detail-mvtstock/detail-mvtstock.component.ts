import { Component, Input, OnInit } from '@angular/core';
import { MvtStockDto } from "../../../gs-api/src";
import {UserService} from "../../services/user/user.service";
import {MvtstockService} from "../../services/mvtstock/mvtstock.service";
import {ArticleService} from "../../services/article/article.service";

/**
 * Configuration des libellés et des sens de mouvement (Entrée/Sortie)
 * On utilise le Namespace généré par OpenAPI pour rester cohérent avec le Backend.
 */
const MVT_CONFIG: Record<MvtStockDto.TypeMvtEnum, { label: string, isEntree: boolean }> = {
  [MvtStockDto.TypeMvtEnum.Entree]: { label: 'ACHAT FOURNISSEUR', isEntree: true },
  [MvtStockDto.TypeMvtEnum.Sortie]: { label: 'VENTE CLIENT', isEntree: false },
  [MvtStockDto.TypeMvtEnum.CorrectionPos]: { label: 'CORRECTION (+)', isEntree: true },
  [MvtStockDto.TypeMvtEnum.CorrectionNeg]: { label: 'CORRECTION (-)', isEntree: false },
  [MvtStockDto.TypeMvtEnum.CorrectionPosVenteRed]: { label: 'CORRECTION (+) VENTE', isEntree: true },
  [MvtStockDto.TypeMvtEnum.CorrectionNegVenteAug]: { label: 'CORRECTION (-) VENTE', isEntree: false }
};

@Component({
  selector: 'app-detail-mvtstock',
  templateUrl: './detail-mvtstock.component.html',
  styleUrls: ['./detail-mvtstock.component.scss']
})
export class DetailMvtstockComponent implements OnInit {

  @Input() mvtStockDto: MvtStockDto = {};

  constructor(
    private userService: UserService,
    private mvtStockService: MvtstockService,
    private articleService: ArticleService
  ) {}

  ngOnInit(): void {}

  /**
   * Automatisation du sens du mouvement
   * Retourne true pour les entrées/corrections positives, false pour les sorties/négatives
   */
  get isEntree(): boolean {
    const type = this.mvtStockDto.typeMvt;
    return type ? MVT_CONFIG[type]?.isEntree : false;
  }

  /**
   * Automatisation du libellé
   * Plus de switch : on pioche directement dans la configuration
   */
  get typeMvtLabel(): string {
    const type = this.mvtStockDto.typeMvt;
    return type ? (MVT_CONFIG[type]?.label || type) : 'INCONNU';
  }

  /**
   * Bonus : Automatisation de la Source (Commande, Vente, etc.)
   */
  get sourceMvtLabel(): string {
    const source = this.mvtStockDto.sourceMvt;
    if (!source) return 'N/A';
    // On peut transformer 'COMMANDE_CLIENT' en 'Commande Client' proprement
    return source.replace(/_/g, ' ');
  }
}





// import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
// import {MvtStockDto} from "../../../gs-api/src";
//
// @Component({
//   selector: 'app-detail-mvtstock',
//   templateUrl: './detail-mvtstock.component.html',
//   styleUrls: ['./detail-mvtstock.component.scss']
// })
// export class DetailMvtstockComponent implements OnInit {
//
//   @Input() mvtStockDto: MvtStockDto = {}; // Utilisation directe du DTO de mouvement
//
//   constructor(
//   ) {}
//
//   ngOnInit(): void {
//     console.log('Données reçues dans le détail :', this.mvtStockDto);
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
//       case 'ENTREE': return 'ACHAT FOURNISSEUR';
//       case 'SORTIE': return 'VENTE CLIENT';
//       case 'CORRECTION_POS': return 'CORRECTION(+)';
//       case 'CORRECTION_NEG': return 'CORRECTION(-)';
//       case 'CORRECTION_POS_VENTE_RED': return 'CORRECTION(+)VENTE';
//       case 'CORRECTION_NEG_VENTE_AUG': return 'CORRECTION(-)VENTE';
//       default: return this.mvtStockDto.typeMvt || 'INCONNU';
//     }
//   }
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

