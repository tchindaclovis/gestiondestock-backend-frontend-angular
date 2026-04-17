import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import {ArticleDto, MvtStockDto, MvtStocksService} from '../../../gs-api/src';

@Injectable({
  providedIn: 'root'
})
export class MvtstockService {

  constructor(
    private mvtStocksService: MvtStocksService
  ) { }

  /**
   * Équivalent de stockReelArticle(Integer idArticle)
   * Récupère la valeur numérique du stock actuel
   */
  stockReelArticle(idArticle: number): Observable<number> {
    if (!idArticle) {
      return of(-1);
    }
    return this.mvtStocksService.stockReelArticle(idArticle);
  }

  /**
   * Équivalent de mvtStockArticle(Integer idArticle)
   * Récupère l'historique complet des mouvements pour un article
   */
  mvtStockArticle(idArticle: number): Observable<Array<MvtStockDto>> {
    if (!idArticle) {
      return of([]);
    }
    return this.mvtStocksService.mvtStockArticle(idArticle);
  }

  /**
   * Équivalent de entreeStock(MvtStockDto dto)
   */
  entreeStock(mvtStockDto: MvtStockDto): Observable<MvtStockDto> {
    return this.mvtStocksService.entreeStock(mvtStockDto);
  }

  /**
   * Équivalent de sortieStock(MvtStockDto dto)
   */
  sortieStock(mvtStockDto: MvtStockDto): Observable<MvtStockDto> {
    return this.mvtStocksService.sortieStock(mvtStockDto);
  }

  /**
   * Équivalent de correctionStockPos(MvtStockDto dto)
   */
  correctionStockPos(mvtStockDto: MvtStockDto): Observable<MvtStockDto> {
    return this.mvtStocksService.correctionStockPos(mvtStockDto);
  }

  /**
   * Équivalent de correctionStockNeg(MvtStockDto dto)
   */
  correctionStockNeg(mvtStockDto: MvtStockDto): Observable<MvtStockDto> {
    return this.mvtStocksService.correctionStockNeg(mvtStockDto);
  }



  findAllMvtStock(): Observable<MvtStockDto[]>{  //renvoit un observable de listes d'articleDto
    return this.mvtStocksService.findAllMvtStock();
  }
}







// import { Injectable } from '@angular/core';
// import { MvtStockDto, MvtStocksService } from "../../../gs-api/src";
// import { Observable, of } from "rxjs";
//
// @Injectable({
//   providedIn: 'root'
// })
// export class MvtstockService {
//
//   constructor(
//     private mvtStocksService: MvtStocksService
//   ) { }
//
//   // Récupère l'historique des mouvements pour un article
//   mvtStockByArticle(idArticle?: number): Observable<Array<MvtStockDto>> {
//     if (idArticle) {
//       return this.mvtStocksService.mvtStockArticle(idArticle);
//     }
//     return of([]);
//   }
//
//   // Récupère le stock réel (calculé par le backend via repository.stockReelArticle)
//   getRealStockArticle(idArticle?: number): Observable<number> {
//     if (idArticle) {
//       return this.mvtStocksService.stockReelArticle(idArticle);
//     }
//     return of(0);
//   }
//
//   // Méthodes pour les corrections (optionnel si vous en avez besoin)
//   correctionPositive(mvt: MvtStockDto): Observable<MvtStockDto> {
//     return this.mvtStocksService.correctionStockPos(mvt);
//   }
//
//   correctionNegative(mvt: MvtStockDto): Observable<MvtStockDto> {
//     return this.mvtStocksService.correctionStockNeg(mvt);
//   }
// }



