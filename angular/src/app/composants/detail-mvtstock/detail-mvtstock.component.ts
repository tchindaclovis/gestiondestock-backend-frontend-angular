import {Component, Input, OnInit} from '@angular/core';
import {LigneCommandeClientDto} from "../../../gs-api/src";

@Component({
  selector: 'app-detail-mvtstock',
  templateUrl: './detail-mvtstock.component.html',
  styleUrls: ['./detail-mvtstock.component.scss']
})
export class DetailMvtstockComponent implements OnInit {

  @Input()
  origin = '';
  @Input()
  commande: any = {};
  clientFournisseur: any | undefined = {};
  @Input()
  ligneCommande: any = {};

  constructor() {}

  ngOnInit(): void {
  }

}
