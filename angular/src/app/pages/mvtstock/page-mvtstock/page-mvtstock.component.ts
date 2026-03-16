import { Component, OnInit } from '@angular/core';
import {LigneCommandeClientDto, MvtStockDto} from "../../../../gs-api/src";

@Component({
  selector: 'app-page-mvtstock',
  templateUrl: './page-mvtstock.component.html',
  styleUrls: ['./page-mvtstock.component.scss']
})
export class PageMvtstockComponent implements OnInit {

  listeMouvements: Array<any> = [];
  mouvementsStock: Array<MvtStockDto> = [];

  mapMouvementsStock = new Map();
  constructor() { }

  ngOnInit(): void {
  }

}
