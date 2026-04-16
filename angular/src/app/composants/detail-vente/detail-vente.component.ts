import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {VenteService} from "../../services/vente/vente.service";

@Component({
  selector: 'app-detail-vente',
  templateUrl: './detail-vente.component.html',
  styleUrls: ['./detail-vente.component.scss']
})
export class DetailVenteComponent implements OnInit {

  @Input()
  origin = ''; // On peut aussi le passer par @Input() depuis le parent pour plus de rapidité

  @Input()
  ligneVente: any = {};

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    // On garde ceci par sécurité, mais l'Input prendra le dessus
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['client'];
    });
  }

  // Méthode de secours pour calculer le total proprement
  calculerTotalLigne(): number {
    if (!this.ligneVente || !this.ligneVente.quantite) {
      return 0;
    }
    // On cherche le prix là où il se trouve (priorité au prix figé de la ligne)
    // Ordre de priorité des champs de prix selon la provenance des données
    const prix = this.ligneVente.prixVenteUnitaireTtc;

    return +prix * +this.ligneVente.quantite;
  }
}
