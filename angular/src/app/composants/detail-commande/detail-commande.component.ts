import {Component, Input, OnInit} from '@angular/core';
import {LigneCommandeClientDto} from "../../../gs-api/src";

@Component({
  selector: 'app-detail-commande',
  templateUrl: './detail-commande.component.html',
  styleUrls: ['./detail-commande.component.scss']
})
export class DetailCommandeComponent implements OnInit {

  @Input()
  // ligneCommande: LigneCommandeClientDto = {};
  ligneCommande: any = {};


  constructor() { }

  ngOnInit(): void {
  }

}
