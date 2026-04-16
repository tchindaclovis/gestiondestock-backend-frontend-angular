import {Component, Input, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";

@Component({
  selector: 'app-detail-commande',
  templateUrl: './detail-commande.component.html',
  styleUrls: ['./detail-commande.component.scss']
})

export class DetailCommandeComponent implements OnInit {

  @Input()
  origin = ''; // On peut aussi le passer par @Input() depuis le parent pour plus de rapidité

  @Input()
  ligneCommande: any = {};

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
  ) { }

  ngOnInit(): void {
    // On garde ceci par sécurité, mais l'Input prendra le dessus
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['origin'];
    });
  }

  // Méthode de secours pour calculer le total proprement
  calculerTotalLigne(): number {
    if (!this.ligneCommande || !this.ligneCommande.quantite) {
      return 0;
    }
      const prix = (this.origin === 'client') ?
        this.ligneCommande.prixVenteUnitaireTtc :
        this.ligneCommande.prixUnitaireTtc;

      return +prix * +this.ligneCommande.quantite;


    // // On cherche le prix là où il se trouve (priorité au prix figé de la ligne)
    // // Ordre de priorité des champs de prix selon la provenance des données
    // const prix = this.ligneCommande.prixUnitaire           // Provient de l'API (LigneCommandeDto)
    //   || this.ligneCommande.prixVenteUnitaireTtc  // Provient du formulaire (ArticleDto - Client)
    //   || this.ligneCommande.prixUnitaireTtc       // Provient du formulaire (ArticleDto - Fournisseur)
    //   || 0;
    //
    // return +prix * +this.ligneCommande.quantite;
  }
}








// import {Component, Input, OnInit} from '@angular/core';
// import {ActivatedRoute} from "@angular/router";
//
// @Component({
//   selector: 'app-detail-commande',
//   templateUrl: './detail-commande.component.html',
//   styleUrls: ['./detail-commande.component.scss']
// })
// export class DetailCommandeComponent implements OnInit {
//
//   @Input()
//   origin = ''; // On peut aussi le passer par @Input() depuis le parent pour plus de rapidité
//
//   @Input()
//   // ligneCommande: LigneCommandeClientDto = {};
//   ligneCommande: any = {};
//
//
//   constructor(
//     private activatedRoute: ActivatedRoute,
//
//   ) { }
//
//   ngOnInit(): void {
//     // 1. Déterminer l'origine
//     this.activatedRoute.data.subscribe(data => {
//       this.origin = data['origin'];
//     })
//   }
// }
