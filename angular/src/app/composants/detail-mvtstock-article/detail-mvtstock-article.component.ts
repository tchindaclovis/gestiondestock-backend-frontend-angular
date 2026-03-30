// ==============================
// IMPORTS ANGULAR
// ==============================

// Component → permet de déclarer un composant Angular
// Input → permet de recevoir des données depuis un composant parent
// OnInit → cycle de vie (initialisation)
// SimpleChanges → détecter les changements sur les @Input
import { Component, Input, OnInit, SimpleChanges } from '@angular/core';


// ==============================
// IMPORTS MODELES & SERVICES API
// ==============================

// ArticleDto → modèle représentant un article
// LigneCommandeClientDto → modèle ligne commande
// MvtStocksService → service API (non utilisé ici)
import {ArticleDto, LigneCommandeClientDto, MvtStockDto, MvtStocksService} from "../../../gs-api/src";


// ==============================
// IMPORT ROUTING
// ==============================

import { ActivatedRoute, Router } from "@angular/router";


// ==============================
// SERVICES METIER
// ==============================

// Service article
import { ArticleService } from "../../services/article/article.service";

// Service commandes client / fournisseur
import { CommandeclientfournisseurService } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";

import {MvtstockService} from "../../services/mvtstock/mvtstock.service";


// ==============================
// DECORATEUR COMPONENT
// ==============================
@Component({
  selector: 'app-detail-mvtstock-article',
  templateUrl: './detail-mvtstock-article.component.html',
  styleUrls: ['./detail-mvtstock-article.component.scss']
})
export class DetailMvtstockArticleComponent implements OnInit {

  // ==============================
  // INPUT
  // ==============================

  /**
   * Article reçu depuis le composant parent
   * Permet de calculer le stock pour cet article précis
   */
  @Input()
  articleDto: ArticleDto = {};

  // ==============================
  // VARIABLES
  // ==============================

  // stock total calculé
  stockGlobal: number = 0;


  // ==============================
  // CONSTRUCTEUR
  // ==============================
  constructor(
    private router: Router,
    private mvtstockService: MvtstockService // Injectez le service de mouvements
  ) { }


  // ==============================
  // INITIALISATION
  // ==============================
  ngOnInit(): void {
    this.chargerStock();
  }

  // ==============================
  // DETECTION DES CHANGEMENTS INPUT
  // ==============================
// Indispensable pour mettre à jour le stock quand on change d'article dans la liste
  /**
   * Si l'article change (Input modifié)
   * → recalcul du stock
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['articleDto'] && this.articleDto?.id) {
      this.chargerStock();
    }
  }

  /**
   * OPTIMISATION : On utilise un seul service dédié aux mouvements de stock
   * au lieu de recalculer à partir de toutes les commandes clients/fournisseurs.
   */
  /**
   * Appelle le service MvtStocks pour récupérer la somme des quantités
   */

  private chargerStock(): void {
    if (this.articleDto && this.articleDto.id) {
      this.mvtstockService.stockReelArticle(this.articleDto.id)
        .subscribe({
          next: (res: any) => {
            // Cas 1 : La réponse est un Blob (votre erreur actuelle)
            if (res instanceof Blob) {
              const reader = new FileReader();
              reader.onload = () => {
                const value = reader.result as string;
                this.stockGlobal = Number(value) || 0;
              };
              reader.readAsText(res);
            }
            // Cas 2 : La réponse est déjà un nombre ou un texte
            else {
              this.stockGlobal = Number(res) || 0;
            }
          },
          error: (err) => {
            console.error("Erreur lors de la récupération du stock", err);
            this.stockGlobal = 0;
          }
        });
    }
  }

}









// // ==============================
// // RECUPERATION DES LIGNES DE COMMANDES (PARALLELE)
// // ==============================
// findAllLigneCommande(): void {
//
//   // Si aucune commande → stock = 0
//   if (this.listeCommandes.length === 0) {
//     this.stockGlobal = 0;
//     return;
//   }
//
//   /**
//    * Création d'un tableau d'observables
//    * pour récupérer les lignes de chaque commande
//    */
//   const observables = this.listeCommandes.map(cmd => {
//     return this.origin === 'client'
//       ? this.commandeClientFournisseurService.findAllLigneCommandesClient(cmd.id)
//       : this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(cmd.id);
//   });
//
//   /**
//    * forkJoin → exécute toutes les requêtes en parallèle
//    * et retourne un tableau de résultats
//    */
//   forkJoin(observables).subscribe({
//
//     next: (results) => {
//
//       let totalCalcule = 0;
//
//       /**
//        * Parcours des résultats
//        */
//       results.forEach((list, index) => {
//
//         const idCommande = this.listeCommandes[index].id;
//
//         // Calcul du total pour cet article dans la commande
//         const totalLignesArt = this.calcTotalStockArt(list);
//
//         // Stockage dans la map
//         this.mapTotalStockArticle.set(idCommande, totalLignesArt);
//
//         /**
//          * LOGIQUE METIER :
//          * - client → sortie de stock (-)
//          * - fournisseur → entrée de stock (+)
//          */
//         if (this.origin === 'client') {
//           totalCalcule -= totalLignesArt;
//         } else if (this.origin === 'fournisseur') {
//           totalCalcule += totalLignesArt;
//         }
//       });
//
//       // Stock global final
//       this.stockGlobal = totalCalcule;
//
//       console.log(`Fin du calcul (${this.origin}):`, this.stockGlobal);
//     },
//
//     error: (err) => console.error("Erreur API:", err)
//   });
// }



// findAllCommandes(): void {
//
//   /**
//    * forkJoin permet d’exécuter plusieurs appels API en parallèle
//    * et d’attendre que tous soient terminés avant de continuer
//    */
//   forkJoin({
//     commandesClient: this.commandeClientFournisseurService.findAllCommandesClient(),
//     commandesFournisseur: this.commandeClientFournisseurService.findAllCommandesFournisseur()
//   }).subscribe({
//
//     next: (result) => {
//
//       /**
//        * Fusion des deux listes :
//        * - commandes client
//        * - commandes fournisseur
//        */
//       this.listeCommandes = [
//         ...(result.commandesClient || []),
//         ...(result.commandesFournisseur || [])
//       ];
//
//       // Charger ensuite les lignes de commandes
//       this.findAllLigneCommande();
//
//       console.log('Commandes fusionnées :', this.listeCommandes);
//     },
//
//     error: (err) => {
//       console.error('Erreur lors du chargement des commandes', err);
//     }
//   });
// }



// // ==============================
// // RECUPERATION DES COMMANDES
// // ==============================
// findAllCommandes(): void {
//
//   /**
//    * Selon l'origine :
//    * - client → commandes client
//    * - fournisseur → commandes fournisseur
//    */
//   if (this.origin === 'client') {
//     this.commandeClientFournisseurService.findAllCommandesClient()
//       .subscribe(cmd => {
//         this.listeCommandes = cmd;
//
//         // Charger ensuite les lignes de commandes
//         this.findAllLigneCommande();
//
//         console.log(cmd);
//       });
//
//   } else if (this.origin === 'fournisseur') {
//     this.commandeClientFournisseurService.findAllCommandesFournisseur()
//       .subscribe(cmd => {
//         this.listeCommandes = cmd;
//
//         this.findAllLigneCommande();
//
//         console.log(cmd);
//       });
//   }
// }


// // ==============================
// // CALCUL DU STOCK POUR UN ARTICLE
// // ==============================
// calcTotalStockArt(list: Array<any>): number {
//
//   let total = 0;
//
//   list.forEach(ligne => {
//
//     /**
//      * Vérifie :
//      * - que la ligne contient un article
//      * - que l’ID correspond à l’article courant
//      * - que la quantité existe
//      */
//     if (
//       ligne.article &&
//       ligne.article.id == this.articleDto.id &&
//       ligne.quantite
//     ) {
//       total += ligne.quantite;
//     }
//   });
//
//   // Arrondi à l'entier inférieur
//   return Math.floor(total);
// }






// import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
// import {ArticleDto, LigneCommandeClientDto, MvtStocksService} from "../../../gs-api/src";
// import {ActivatedRoute, Router} from "@angular/router";
// import {ArticleService} from "../../services/article/article.service";
// import {
//   CommandeclientfournisseurService
// } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
// import { forkJoin } from 'rxjs';
// import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";
//
// @Component({
//   selector: 'app-detail-mvtstock-article',
//   templateUrl: './detail-mvtstock-article.component.html',
//   styleUrls: ['./detail-mvtstock-article.component.scss']
// })
// export class DetailMvtstockArticleComponent implements OnInit {
//
//   @Input()
//   articleDto: ArticleDto = {};
//
//   origin = '';
//   listeCommandes: Array<any> = [];
//   lignesCommande: Array<any> = [];
//   // lignesCommande: Array<LigneCommandeClientDto> = [];
//
//   stockGlobal: number = 0;
//
//   mapLignesCommande = new Map();
//   mapTotalStockArticle = new Map();
//
//   commande: any = {};
//   clientFournisseur: any | undefined = {};
//
//   constructor(
//     private router: Router,
//     private articleService: ArticleService,
//     private activatedRoute: ActivatedRoute,
//     private commandeClientFournisseurService: CommandeclientfournisseurService
//   ) { }
//
//   ngOnInit(): void {
//     this.activatedRoute.data.subscribe(data =>{
//       this.origin = data['origin'];
//     });
//     this.findAllCommandes();
//   }
//
//
//   ngOnChanges(changes: SimpleChanges): void {
//     if (changes['articleDto'] && this.articleDto?.id) {
//       this.findAllCommandes();
//     }
//   }
//
//   // ngOnChanges(): void {
//   //   if (this.articleDto?.id) {
//   //     this.findAllCommandes();
//   //   }
//   // }
//
//
//   findAllCommandes(): void {
//     if(this.origin === 'client') {
//       this.commandeClientFournisseurService.findAllCommandesClient()
//         .subscribe(cmd => {
//           this.listeCommandes = cmd;
//           this.findAllLigneCommande();
//           console.log(cmd)
//         });
//
//     }else  if(this.origin === 'fournisseur'){
//       this.commandeClientFournisseurService.findAllCommandesFournisseur()
//         .subscribe(cmd => {
//           this.listeCommandes = cmd;
//           this.findAllLigneCommande();
//           console.log(cmd)
//         });
//     }
//   }
//
//
//
//   findAllLigneCommande(): void {
//     if (this.listeCommandes.length === 0) {
//       this.stockGlobal = 0;
//       return;
//     }
//
//     const observables = this.listeCommandes.map(cmd => {
//       return this.origin === 'client'
//         ? this.commandeClientFournisseurService.findAllLigneCommandesClient(cmd.id)
//         : this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(cmd.id);
//     });
//
//     forkJoin(observables).subscribe({
//       next: (results) => {
//         let totalCalculé = 0;
//
//         results.forEach((list, index) => {
//           const idCommande = this.listeCommandes[index].id;
//           const totalLignesArt = this.calcTotalStockArt(list);
//
//           this.mapTotalStockArticle.set(idCommande, totalLignesArt);
//
//           // LOGIQUE DE CALCUL SELON L'ORIGINE
//           if (this.origin === 'client') {
//             // Si c'est un client, c'est une sortie de stock (SOUSTRACTION)
//             totalCalculé -= totalLignesArt;
//           } else if (this.origin === 'fournisseur') {
//             // Si c'est un fournisseur, c'est une entrée de stock (ADDITION)
//             totalCalculé += totalLignesArt;
//           }
//         });
//
//         this.stockGlobal = totalCalculé;
//         console.log(`Fin du calcul (${this.origin}):`, this.stockGlobal);
//       },
//       error: (err) => console.error("Erreur API:", err)
//     });
//   }
//
//
//
//   findLignesCommande(idCommande?: number): void {
//     if(this.origin === 'client'){
//       this.commandeClientFournisseurService.findAllLigneCommandesClient(idCommande)
//         .subscribe(list => {
//           this.mapLignesCommande.set(idCommande, list); //la clé est l'idCommande et la valeur est la liste des lignes commandes
//           const total = this.calcTotalStockArt(list);
//           this.mapTotalStockArticle.set(idCommande, total);
//           // 🔥 recalcul du stock global
//           this.calculStockGlobal();
//         });
//     } else if(this.origin === 'fournisseur'){
//       this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(idCommande)
//         .subscribe(list => {
//           this.mapLignesCommande.set(idCommande, list); //la clé est l'idCommande et la valeur est la liste des lignes commandes
//           const total = this.calcTotalStockArt(list);
//           this.mapTotalStockArticle.set(idCommande, total);
//           // 🔥 recalcul du stock global
//           this.calculStockGlobal();
//         });
//     }
//   }
//
//
//   calculStockGlobal(): void {
//     let total = 0;
//
//     this.mapTotalStockArticle.forEach(value => {
//       if (value) {
//         total += value;
//
//         // if(this.origin === 'client') {
//         //   total -= value;
//         // }else if(this.origin === 'fournisseur') {
//         //   total += value;
//         // }
//       }
//     });
//     this.stockGlobal = total;
//   }
//
//
//   /**
//    * Compare les IDs de manière sécurisée (cast en string)
//    */
//   calcTotalStockArt(list: Array<any>): number {
//     let total = 0;
//     list.forEach(ligne => {
//       // Utilisation de == pour éviter les problèmes de type string/number
//       // Et vérification de l'existence de l'ID
//       if (ligne.article &&
//         ligne.article.id == this.articleDto.id &&
//         ligne.quantite) {
//         total += ligne.quantite;
//       }
//     });
//     return Math.floor(total);
//   }
//
//
//   calculerTotalStockArticle(id?: number): number {
//     return this.mapTotalStockArticle.get(id);
//   }
//
// }
