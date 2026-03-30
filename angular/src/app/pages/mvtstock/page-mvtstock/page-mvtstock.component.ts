import {Component, Input, OnInit} from '@angular/core';
import { ArticleDto, MvtStockDto } from "../../../../gs-api/src";
import { ArticleService } from "../../../services/article/article.service";
import { MvtstockService } from "../../../services/mvtstock/mvtstock.service";

@Component({
  selector: 'app-page-mvtstock',
  templateUrl: './page-mvtstock.component.html',
  styleUrls: ['./page-mvtstock.component.scss']
})
export class PageMvtstockComponent implements OnInit {

  listArticle: Array<ArticleDto> = [];
  // Map pour stocker les mouvements par ID d'article : Key = idArticle, Value = MvtStockDto[]
  mapMouvementsStock = new Map<number, Array<MvtStockDto>>();
  // mapMouvementsStock = new Map();

  constructor(
    private mvtStockService: MvtstockService,
    private articleService: ArticleService
  ) { }

  ngOnInit(): void {
    this.findAllArticlesAndMvts();
    // this.findAllMvtStockArt()
  }


  // findAllMvtStockArt(): void {
  //   this.listeMvtStocks.forEach(article =>{
  //     this.findMvtStockArt(article.id)
  //   });
  // }
  //
  //
  // findMvtStockArt(idArticle: number): void {
  //     this.mvtStockService.mvtStockArticle(idArticle)
  //       .subscribe(list => {
  //         this.mapMouvementsStock.set(idArticle, list); //la clé est l'idArticle et la valeur est la liste des mouvements de stock
  //       });
  // }


  findAllArticlesAndMvts(): void {
    this.articleService.findAllArticle().subscribe(articles => {
      this.listArticle = articles;
      // Pour chaque article récupéré, on charge ses mouvements de stock
      articles.forEach(art => {
        if (art.id) {
          this.findMvtsArticle(art.id);
        }
      });
    });
  }



  findMvtsArticle(idArticle: number): void {
    this.mvtStockService.mvtStockArticle(idArticle).subscribe((res: any) => {

      // Testez si c'est un Blob
      if (res instanceof Blob) {
        const reader = new FileReader();
        reader.onload = () => {
          const jsonRes = JSON.parse(reader.result as string);
          this.mapMouvementsStock.set(idArticle, jsonRes);
        };
        reader.readAsText(res);
      } else {
        // Si c'est déjà un tableau
        this.mapMouvementsStock.set(idArticle, res);
      }

    });
  }


  // findMvtsArticle(idArticle: number): void {
  //   this.mvtStockService.mvtStockArticle(idArticle).subscribe(mvts => {
  //     // On crée une nouvelle instance de Map pour forcer le rafraîchissement
  //     this.mapMouvementsStock.set(idArticle, mvts);
  //     this.mapMouvementsStock = new Map(this.mapMouvementsStock);
  //   });
  // }

  // findMvtsArticle(idArticle: number): void {
  //   this.mvtStockService.mvtStockArticle(idArticle).subscribe(mvts => {
  //     this.mapMouvementsStock.set(idArticle, mvts);
  //   });
  // }
}










// findAllArticlesAndMvts(): void {
//   this.articleService.findAllArticle().subscribe(articles => {
//     this.listArticle = articles;
//     articles.forEach(art => {
//       if (art.id) {
//         this.mapMouvementsStock.set(art.id, []); // Initialise à vide
//         this.findMvtsArticle(art.id);
//       }
//     });
//   });
// }




// import {Component, Input, OnInit} from '@angular/core';
// import {ArticleDto, LigneCommandeClientDto, MvtStockDto} from "../../../../gs-api/src";
// import {Router} from "@angular/router";
// import {ArticleService} from "../../../services/article/article.service";
// import {MvtstockService} from "../../../services/mvtstockarticle/mvtstock.service";
//
// @Component({
//   selector: 'app-page-mvtstock',
//   templateUrl: './page-mvtstock.component.html',
//   styleUrls: ['./page-mvtstock.component.scss']
// })
// export class PageMvtstockComponent implements OnInit {
//
//   origin = '';
//   listeCommandes: Array<any> = [];
//   lignesCommande: Array<any> = [];
//   // lignesCommande: Array<LigneCommandeClientDto> = [];
//
//   mapMouvementsStock = new Map();
//   mapPrixTotalCommande = new Map();
//
//   listArticle: Array<ArticleDto> = [];
//   listMvtstockart: Array<MvtStockDto> = [];
//   errorMsg = '';
//
//   constructor(
//     private router: Router,
//     private mvtStockService: MvtstockService,
//     private articleService: ArticleService
//   ) { }
//
//   ngOnInit(): void {
//     this.findListArticle()
//     this.findListMvtStockArticle()
//   }
//
//   findListArticle(): void{
//     this.articleService.findAllArticle()
//       .subscribe(articles =>{
//         this.listArticle = articles;
//       });
//   }
//
//   findListMvtStockArticle(): void{
//     this.mvtStockService.mvtStockByArticle()
//       .subscribe(mvtstockarts =>{
//         this.listMvtstockart = mvtstockarts;
//       });
//   }
// }
