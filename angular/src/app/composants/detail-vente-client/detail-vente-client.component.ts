import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";
import {VenteService} from "../../services/vente/vente.service";
import {
  CommandeclientfournisseurService
} from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
import {VenteDto} from "../../../gs-api/src";

@Component({
  selector: 'app-detail-vente-client',
  templateUrl: './detail-vente-client.component.html',
  styleUrls: ['./detail-vente-client.component.scss']
})
export class DetailVenteClientComponent implements OnInit {

  @Input() origin = '';

  @Input() vente: any = {};

  clientFournisseur: any = {};

  @Output()
  suppressionResult = new EventEmitter<string>();

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private commandeClientFournisseurService: CommandeclientfournisseurService,
    private venteService: VenteService
  ) { }

  ngOnInit(): void {
    // On s'abonne aux data de la route pour connaître l'origine (client ou fournisseur)
    this.activatedRoute.data.subscribe(data => {
      this.origin = 'client';
      this.extractClientFournisseur();
    });
  }

  // Crucial : Détecter quand l'objet 'vente' arrive du composant parent
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['vente']) {
      this.extractClientFournisseur();
    }
  }

  extractClientFournisseur(): void {
    if (this.vente) {
      this.clientFournisseur = this.vente.client;
    }
  }

  modifierVente(): void {
    const route = 'nouvellevente';
    // On navigue vers la page de modification avec l'ID de la commande
    this.router.navigate([route, this.vente.id]);
  }


  confirmerEtSupprimer(): void {
    if (this.vente && this.vente.id) {
      this.venteService.deleteVente(this.vente.id)
        .subscribe({
          next: () => {
            // 1. On émet d'abord le succès pour les éventuels composants parents
            this.suppressionResult.emit('success');

            // 2. On attend la fermeture de la modal (300ms) avant de naviguer
            setTimeout(() => {
              // 3. Navigation vers la liste des ventes
              // Cela déclenchera le rechargement du composant de la liste
              this.router.navigate(['ventes']);
            }, 300);
          },
          error: (err) => {
            console.error("Erreur suppression:", err);
            this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression');
          }
        });
    }
  }


  // confirmerEtSupprimer(): void {
  //   if (this.vente && this.vente.id) {
  //     this.venteService.deleteVente(this.vente.id)
  //       .subscribe({
  //         next: () => {
  //           // On informe le composant parent que la suppression a réussi
  //           // Le parent devra alors rafraîchir la liste des ventes
  //           this.suppressionResult.emit('success');
  //         },
  //         error: (err) => {
  //           console.error("Erreur lors de la suppression de la vente", err);
  //           this.suppressionResult.emit(err.error.message || 'Erreur lors de la suppression');
  //         }
  //       });
  //   } else {
  //     console.error("Impossible de supprimer : ID manquant");
  //   }
  // }

}









