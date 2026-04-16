import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {
  CommandeclientfournisseurService
} from "../../../services/commandeclientfournisseur/commandeclientfournisseur.service";
import {VenteService} from "../../../services/vente/vente.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-page-vente',
  templateUrl: './page-vente.component.html',
  styleUrls: ['./page-vente.component.scss']
})
export class PageVenteComponent implements OnInit {

  @Input() origin = '';
  listeVentes: Array<any> = [];
  // Initialisation de la Map pour éviter les erreurs de lecture immédiate dans le template
  mapLignesVente = new Map<number, any[]>();
  mapPrixTotalVente = new Map<number, number>();
  errorMsg : Array<string> = [];

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private venteService: VenteService
  ) { }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data =>{
      this.origin = data['client'];
      this.findAllVentes(); // Déplacé ici pour garantir que l'origin est chargé
    });
  }

  findAllVentes(): void {
    let serviceCall: Observable<any>;
    serviceCall = this.venteService.findAllVentes();
    serviceCall.subscribe(cmd => {
      this.listeVentes = cmd;
      // Initialiser la Map avec des tableaux vides pour CHAQUE vente immédiatement
      this.listeVentes.forEach(c => {
        this.mapLignesVente.set(c.id, []);
        this.mapPrixTotalVente.set(c.id, 0);
      });
      this.findAllLigneVente();
    });
  }

  findAllLigneVente(): void {
    this.listeVentes.forEach(vte => {
      this.findLignesVente(vte.id);
    });
  }

  findLignesVente(idVente: number): void {
    // 1. Déclaration avec initialisation pour éviter l'erreur "used before being assigned"
    let serviceLignes: Observable<any>;
      serviceLignes = this.venteService.findAllLigneVentes(idVente);
    serviceLignes.subscribe(list => {
      // Si le retour est un Blob (fréquent dans votre projet), on le gère
      if (list instanceof Blob) {
        list.text().then(text => {
          const parsedList = JSON.parse(text);
          this.mapLignesVente.set(idVente, parsedList);
          this.mapPrixTotalVente.set(idVente, this.calcTotalVte(parsedList));
        });
      } else {
        this.mapLignesVente.set(idVente, list || []);
        this.mapPrixTotalVente.set(idVente, this.calcTotalVte(list || []));
      }
    });
  }

  calcTotalVte(list: Array<any>): number {
    let total = 0;
    if (list) {
      list.forEach(ligne => {
        // Utilisation du prix unitaire ou prix de vente selon l'objet
        const pu = ligne.prixUnitaire  || ligne.prixVenteUnitaireTtc|| ligne.prixUnitaireTtc  || 0;
        total += (+pu * +ligne.quantite);
      });
    }
    return Math.floor(total);
  }

  calculerTotalVente(id: number): number {
    return this.mapPrixTotalVente.get(id) || 0;
  }

  nouvelleVente(): void {
    this.router.navigate(['nouvellevente']);
  }


  handleSuppression(result: string): void {
    if (result === 'success') {
      // On appelle simplement la méthode qui charge vos ventes
      // Cela mettra à jour la liste 'ventes' et Angular rafraîchira l'écran instantanément
      this.findAllVentesClient();
    } else {
      // // Optionnel : afficher un message d'erreur si le signal n'est pas 'success'
      // this.errorMsg = result;
    }
  }

  findAllVentesClient(): void {
    this.venteService.findAllVentes().subscribe(res => {
      this.listeVentes = res;
    });
  }

}
