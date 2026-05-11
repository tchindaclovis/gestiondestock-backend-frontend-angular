import {Component, Input, OnInit} from '@angular/core';
import {ArticleDto, MvtStockDto, UtilisateurDto} from "../../../../gs-api/src";
import { ArticleService } from "../../../services/article/article.service";
import { MvtstockService } from "../../../services/mvtstock/mvtstock.service";
import {UserService} from "../../../services/user/user.service";
import {VenteService} from "../../../services/vente/vente.service";

@Component({
  selector: 'app-page-mvtstock',
  templateUrl: './page-mvtstock.component.html',
  styleUrls: ['./page-mvtstock.component.scss']
})
export class PageMvtstockComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;
  listArticle: Array<ArticleDto> = [];
  listeMvts: Array<any> = [];

  // Map pour stocker les mouvements par ID d'article : Key = idArticle, Value = MvtStockDto[]
  mapMouvementsStock = new Map<number, Array<MvtStockDto>>();
  errorMsg = '';

  constructor(
    private userService: UserService,
    private mvtStockService: MvtstockService,
    private articleService: ArticleService,
    private venteService: VenteService,
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();
    this.findAllArticlesAndMvts();
    // findAllVentes();
  }


  // // Dans page-mvtstock.component.ts
  // findAllArticlesAndMvts(): void {
  //   const idEntreprise = this.connectedUser?.entreprise?.id;
  //   if (!idEntreprise) return;
  //
  //   this.articleService.findAllArticlesByIdEntreprise(idEntreprise).subscribe(articles => {
  //     this.listArticle = articles;
  //
  //     this.mvtStockService.findAllMvtsByEntreprise(idEntreprise).subscribe(allMvts => {
  //       // 1. Utiliser une Map temporaire
  //       const tempMap = new Map<number, Array<MvtStockDto>>();
  //
  //       allMvts.forEach(mvt => {
  //         // Forcer la conversion en Number au cas où le backend renvoie un String
  //         const artId = Number(mvt.article?.id);
  //
  //         if (!isNaN(artId)) {
  //           if (!tempMap.has(artId)) {
  //             tempMap.set(artId, []);
  //           }
  //           tempMap.get(artId)?.push(mvt);
  //         }
  //       });
  //
  //       // 2. Réassigner la Map entière pour déclencher la détection de changement
  //       this.mapMouvementsStock = new Map(tempMap);
  //       console.log('Map mise à jour :', this.mapMouvementsStock);
  //     });
  //   });
  // }


  findAllArticlesAndMvts(): void {
    const idEntreprise = this.connectedUser?.entreprise?.id;
    console.log("ID Entreprise utilisé :", idEntreprise);

    if (!idEntreprise) return;

    // Utilisation de forkJoin pour attendre que les deux appels soient finis
    // OU charger les articles, puis charger TOUS les mouvements d'un coup
    this.articleService.findAllArticlesByIdEntreprise(idEntreprise).subscribe(articles => {
      this.listArticle = articles;

      this.mvtStockService.findAllMvtsByEntreprise(idEntreprise).subscribe(allMvts => {
        // 1. On travaille sur une Map locale temporaire
        const tempMap = new Map<number, Array<MvtStockDto>>();

        allMvts.forEach(mvt => {
          const artId = mvt.article?.id;
          if (artId !== undefined && artId !== null) {
            if (!tempMap.has(artId)) {
              tempMap.set(artId, []);
            }
            tempMap.get(artId)?.push(mvt);
          }
        });

        // 2. ON ASSIGNE LA NOUVELLE MAP (Cela déclenche le rafraîchissement Angular)
        this.mapMouvementsStock = tempMap;
      });

    });
  }


  // findAllArticlesAndMvts(): void {
//   const idEntreprise = this.connectedUser?.entreprise?.id;
//   console.log("ID Entreprise utilisé :", idEntreprise);
//
//   if (!idEntreprise) return;
//
//   // Utilisation de forkJoin pour attendre que les deux appels soient finis
//   // OU charger les articles, puis charger TOUS les mouvements d'un coup
//   this.articleService.findAllArticlesByIdEntreprise(idEntreprise).subscribe(articles => {
//     this.listArticle = articles;
//
//     // UNE SEULE REQUÊTE au lieu d'une boucle foreach
//     // 1. On s'assure que l'entreprise est définie
//     this.mvtStockService.findAllMvtsByEntreprise(idEntreprise).subscribe(allMvts => {
//       // On vide la map
//       this.mapMouvementsStock.clear();
//
//       // On ventile les mouvements reçus dans la Map par ID d'article
//       allMvts.forEach(mvt => {
//         // On vérifie que l'article et son ID existent bien avant de continuer
//         const artId = mvt.article?.id;
//
//         if (artId !== undefined && artId !== null) {
//           // Ici, TypeScript sait que artId est obligatoirement un 'number'
//           if (!this.mapMouvementsStock.has(artId)) {
//             this.mapMouvementsStock.set(artId, []);
//           }
//           this.mapMouvementsStock.get(artId)?.push(mvt);
//         }
//       });
//
//     });
//   });
// }


  handleCorrection(result: string): void {
    if (result === 'success') {
      // On appelle simplement la méthode qui charge vos ventes
      // Cela mettra à jour la liste 'ventes' et Angular rafraîchira l'écran instantanément
      this.findAllArticlesAndMvts();
      // 2. Optionnel : On peut aussi forcer le rafraîchissement de la liste globale
      this.findAllMvtStock()

      console.log('Mouvements mis à jour avec succès');
    }
  }

  findAllMvtStock(): void {
    this.mvtStockService.findAllMvtStock().subscribe(res => {
      this.listeMvts= res;
    });
  }

  // Nous allons créer une propriété calculée (getter) qui additionne la taille
  // de chaque liste de mouvements stockée dans votre mapMouvementsStock.
  // Remplacez ou ajoutez ce getter pour calculer le total dynamiquement
  get totalMouvements(): number {
    let total = 0;
    this.mapMouvementsStock.forEach((liste) => {
      total += liste.length;
    });
    return total;
  }
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
