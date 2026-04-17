import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MvtStockDto} from "../../../gs-api/src";

@Component({
  selector: 'app-detail-mvtstock',
  templateUrl: './detail-mvtstock.component.html',
  styleUrls: ['./detail-mvtstock.component.scss']
})
export class DetailMvtstockComponent implements OnInit {

  @Input() mvtStockDto: MvtStockDto = {}; // Utilisation directe du DTO de mouvement

  constructor(
  ) {}

  ngOnInit(): void {
    console.log('Données reçues dans le détail :', this.mvtStockDto);
  }

  // Détermine si le mouvement est une Entrée ou une Sortie
  get isEntree(): boolean {
    return this.mvtStockDto.typeMvt === 'ENTREE' || this.mvtStockDto.typeMvt === 'CORRECTION_POS';
  }

  // Retourne un libellé propre selon le type enum
  get typeMvtLabel(): string {
    switch (this.mvtStockDto.typeMvt) {
      case 'ENTREE': return 'ACHAT FOURNISSEUR';
      case 'SORTIE': return 'VENTE CLIENT';
      case 'CORRECTION_POS': return 'CORRECTION (+)';
      case 'CORRECTION_NEG': return 'CORRECTION (-)';
      default: return this.mvtStockDto.typeMvt || 'INCONNU';
    }
  }
}








// import {Component, Input, OnInit} from '@angular/core';
//
// @Component({
//   selector: 'app-detail-mvtstock',
//   templateUrl: './detail-mvtstock.component.html',
//   styleUrls: ['./detail-mvtstock.component.scss']
// })
// export class DetailMvtstockComponent implements OnInit {
//
//   @Input()
//   origin = '';
//   @Input()
//   commande: any = {};
//   clientFournisseur: any | undefined = {};
//   @Input()
//   ligneCommande: any = {};
//
//   constructor() {}
//
//   ngOnInit(): void {
//   }
// }
