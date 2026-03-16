import {Component, Input, OnInit} from '@angular/core';
import {ArticleDto, MvtStocksService} from "../../../gs-api/src";
import {Router} from "@angular/router";
import {ArticleService} from "../../services/article/article.service";

@Component({
  selector: 'app-detail-mvtstock-article',
  templateUrl: './detail-mvtstock-article.component.html',
  styleUrls: ['./detail-mvtstock-article.component.scss']
})
export class DetailMvtstockArticleComponent implements OnInit {
  @Input()
  origin = '';
  @Input()
  ligneCommande: any = {};

  @Input()
  articleDto: ArticleDto = {};
  commande: any = {};
  clientFournisseur: any | undefined = {};

  constructor(
    private router: Router,
    private articleService: ArticleService,
    private mvtStocksService: MvtStocksService
  ) { }

  ngOnInit(): void {
    this.extractClientFournisseur();
  }

  extractClientFournisseur(): void {
    if(this.origin === 'client'){
      this.clientFournisseur = this.commande?.client;
    } else if(this.origin === 'fournisseur'){
      this.clientFournisseur = this.commande?.fournisseur;
    }
  }
}
