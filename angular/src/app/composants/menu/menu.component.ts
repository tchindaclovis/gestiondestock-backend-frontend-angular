import { Component, OnInit } from '@angular/core';
import {Menu} from "./menu";
import {Router} from "@angular/router";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {
  public menuProperties: Array<Menu> =[
    {
      id: '1',
      titre: 'Tableau de bord',
      icon: 'fa-solid fa-chart-line',
      url: '',
      sousMenu: [
        {
          id: '11',
          titre: 'Vue d\'ensemble',
          icon: 'fa-solid fa-chart-pie',
          url: ''
        },
        {
          id: '12',
          titre: 'Statistiques',
          icon: 'fa-solid fa-chart-column',
          url: 'statistiques'
        }
      ]
    },

    {
      id: '2',
      titre: 'Articles',
      icon: 'fa-solid fa-cart-shopping',
      url: '',
      sousMenu: [
        {
          id: '21',
          titre: 'Articles',
          icon: 'fa-solid fa-gift',
          url: 'articles'
        },
        {
          id: '22',
          titre: 'Mouvements de stock',
          icon: 'fa-solid fa-up-down',
          url: 'mvtstock'
        }
      ]
    },

    {
      id: '3',
      titre: 'Clients',
      icon: 'fa-solid fa-people-group',
      url: '',
      sousMenu: [
        {
          id: '31',
          titre: 'Clients',
          icon: 'fa-solid fa-user-check',
          url: 'clients'
        },
        {
          id: '32',
          titre: 'Commandes clients',
          icon: 'fa-solid fa-person-walking-arrow-right',
          url: 'commandesclient'
        }
      ]
    },

    {
      id: '4',
      titre: 'Fournisseurs',
      icon: 'fa-solid fa-truck-field-un',
      url: '',
      sousMenu: [
        {
          id: '41',
          titre: 'Fournisseurs',
          icon: 'fa-solid fa-building',
          url: 'fournisseurs'
        },
        {
          id: '42',
          titre: 'Commandes fournisseurs',
          icon: 'fa-solid fa-building-circle-arrow-right',
          url: 'commandesfournisseur'
        }
      ]
    },

    {
      id: '5',
      titre: 'Parametrages',
      icon: 'fa-solid fa-gears',
      url: '',
      sousMenu: [
        {
          id: '51',
          titre: 'Categories',
          icon: 'fa-solid fa-list',
          url: 'categories'
        },
        {
          id: '52',
          titre: 'Utilisateurs',
          icon: 'fa-solid fa-person-booth',
          url: 'utilisateurs'
        }
      ]
    },
  ];

  private lastSelectedMenu: Menu | undefined;
  constructor(
    private router: Router
  ) {}

  ngOnInit(): void {
  }

  navigate(menu: Menu): void {
    if (this.lastSelectedMenu){
      this.lastSelectedMenu.active = false;
    }
    menu.active = true;
    this.lastSelectedMenu = menu;
    this.router.navigate([menu.url]);
  }
}
