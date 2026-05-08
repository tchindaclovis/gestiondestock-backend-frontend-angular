import {Component, Input, OnInit, Output, EventEmitter, SimpleChanges} from '@angular/core';
import {ArticleDto, VenteDto} from '../../../gs-api/src';
import { MvtstockService } from '../../services/mvtstock/mvtstock.service';
import {Router} from "@angular/router";
import { MvtStockDto } from "../../../gs-api/src/model/mvtStockDto";
import {ArticleService} from "../../services/article/article.service";


@Component({
  selector: 'app-detail-mvtstock-article',
  templateUrl: './detail-mvtstock-article.component.html',
  styleUrls: ['./detail-mvtstock-article.component.scss']
})
export class DetailMvtstockArticleComponent implements OnInit {

  @Input() articleDto: ArticleDto = {};
  @Input() stockGlobal: number = 0;


  // Objet local pour le formulaire
  mvtStockDto: MvtStockDto = {
    quantite: 0,
    typeMvt: 'CORRECTION_POS', // Valeur par défaut
    sourceMvt: 'VENTE'
  };

  // @Output() suppressionResult = new EventEmitter<string>();

  // Émetteur pour demander au parent de rafraîchir la liste/stock après correction
  @Output() correctionStockEvent = new EventEmitter<string>();

  // Déclarez la variable sans l'initialiser avec Object.values ici
  sourceMvtsOptions: string[] = [];
  typeMvtsOptions: string[] = [];


  constructor(
    private router: Router,
    private mvtstockService: MvtstockService, // Injectez le service de mouvements
    private articleService:ArticleService

  ) { }

  // ==============================
  // INITIALISATION
  // ==============================
  ngOnInit(): void {
    // Initialisez la liste ici pour éviter les erreurs de compilation
    if (MvtStockDto.TypeMvtEnum) {
      this.typeMvtsOptions = Object.values(MvtStockDto.TypeMvtEnum);
    }
    if (MvtStockDto.SourceMvtEnum) {
      this.sourceMvtsOptions = Object.values(MvtStockDto.SourceMvtEnum);
    }
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


  enregistrerCorrection(): void {
  if (!this.articleDto.id || !this.mvtStockDto.quantite) return;

  this.mvtStockDto.article = this.articleDto;
  this.mvtStockDto.dateMvt = new Date().toISOString();

// AJOUTE CETTE LIGNE :
    // On récupère l'idEntreprise de l'article (car l'article appartient à l'entreprise)
    this.mvtStockDto.idEntreprise = this.articleDto.idEntreprise;

  let call;
  // Utilisation du switch case pour déterminer l'appel au service
  switch (this.mvtStockDto.typeMvt) {
    case 'CORRECTION_POS':
      call = this.mvtstockService.correctionStockPos(this.mvtStockDto);
      break;
    case 'CORRECTION_NEG':
      call = this.mvtstockService.correctionStockNeg(this.mvtStockDto);
      break;
    case 'CORRECTION_POS_VENTE_RED':
      call = this.mvtstockService.correctionStockPosVenteRed(this.mvtStockDto);
      break;

    case 'CORRECTION_NEG_VENTE_AUG':
      call = this.mvtstockService.correctionStockNegVenteAug(this.mvtStockDto);
      break;

    case 'CORRECTION_NEG_RETOUR_FOURNISSEUR':
      call = this.mvtstockService.correctionStockNegRetourFournisseur(this.mvtStockDto);
      break;

    default:
      // Optionnel : gérer un cas par défaut ou une erreur
      console.warn('Type de mouvement non reconnu');
      return; // On sort de la méthode si le type est inconnu
  }

    // const call = this.mvtStockDto.typeMvt === 'CORRECTION_POS'
    //   ? this.mvtstockService.correctionStockPos(this.mvtStockDto)
    //   : this.mvtstockService.correctionStockNeg(this.mvtStockDto);

  // Exécution de l'appel API
  call.subscribe({
    next: () => {
      // 1. Réinitialiser le formulaire local
      this.handleSuccess();

      // 2. Recharger le stock global de cet article précisément
      this.chargerStock();

      // 3. Notifier le parent pour qu'il recharge la liste des mouvements
      // On passe l'ID de l'article pour que le parent sache qui mettre à jour
      this.correctionStockEvent.emit('success');
    },
    error: (err) => console.error("Erreur lors de la correction", err)
  });
}

  // enregistrerCorrection(): void {
  //   if (!this.articleDto.id || !this.mvtStockDto.quantite) {
  //     return;
  //   }
  //   // On prépare le DTO avec l'article concerné
  //   this.mvtStockDto.article = this.articleDto;
  //   this.mvtStockDto.dateMvt = new Date().toISOString();
  //
  //   if (this.mvtStockDto.typeMvt === 'CORRECTION_POS') {
  //     this.mvtstockService.correctionStockPos(this.mvtStockDto).subscribe({
  //       next: () => {
  //         this.handleSuccess();
  //         // 1. On émet d'abord le succès pour les éventuels composants parents
  //         // On prévient le parent pour mettre à jour l'affichage du stock actuel
  //         this.correctionStockEvent.emit('success');
  //         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
  //         setTimeout(() => {
  //           // 3. Navigation vers la liste des ventes
  //           // Cela déclenchera le rechargement du composant de la liste
  //           this.router.navigate(['mvtstock']);
  //         }, 300);
  //       },
  //       error: (err) => console.error(err)
  //     });
  //   } else  if (this.mvtStockDto.typeMvt === 'CORRECTION_NEG'){
  //     this.mvtstockService.correctionStockNeg(this.mvtStockDto).subscribe({
  //       next: () => {
  //         this.handleSuccess();
  //         // 1. On émet d'abord le succès pour les éventuels composants parents
  //         // On prévient le parent pour mettre à jour l'affichage du stock actuel
  //         this.correctionStockEvent.emit('success');
  //         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
  //         setTimeout(() => {
  //           // 3. Navigation vers la liste des ventes
  //           // Cela déclenchera le rechargement du composant de la liste
  //           this.router.navigate(['mvtstock']);
  //         }, 300);
  //       },
  //       error: (err) => console.error(err)
  //     });
  //   } else  if (this.mvtStockDto.typeMvt === 'CORRECTION_POS_VENTE_RED'){
  //     this.mvtstockService.correctionStockPosVenteRed(this.mvtStockDto).subscribe({
  //       next: () => {
  //         this.handleSuccess();
  //         // 1. On émet d'abord le succès pour les éventuels composants parents
  //         // On prévient le parent pour mettre à jour l'affichage du stock actuel
  //         this.correctionStockEvent.emit('success');
  //         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
  //         setTimeout(() => {
  //           // 3. Navigation vers la liste des ventes
  //           // Cela déclenchera le rechargement du composant de la liste
  //           this.router.navigate(['mvtstock']);
  //         }, 300);
  //       },
  //       error: (err) => console.error(err)
  //     });
  //   } else  if (this.mvtStockDto.typeMvt === 'CORRECTION_NEG_VENTE_AUG'){
  //     this.mvtstockService.correctionStockNegVenteAug(this.mvtStockDto).subscribe({
  //       next: () => {
  //         this.handleSuccess();
  //         // 1. On émet d'abord le succès pour les éventuels composants parents
  //         // On prévient le parent pour mettre à jour l'affichage du stock actuel
  //         this.correctionStockEvent.emit('success');
  //         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
  //         setTimeout(() => {
  //           // 3. Navigation vers la liste des ventes
  //           // Cela déclenchera le rechargement du composant de la liste
  //           this.router.navigate(['mvtstock']);
  //         }, 300);
  //       },
  //       error: (err) => console.error(err)
  //     });
  //   }
  // }


  private handleSuccess(): void {
    // Réinitialisation du formulaire
    this.mvtStockDto = {
      quantite: 0,
      typeMvt: 'CORRECTION_POS'
    };
  }

  enregistrerQuantiteAlert(): void {
    // On vérifie que l'article et son ID existent bien
    if (this.articleDto && this.articleDto.id) {
      this.articleService.enregistrerArticle(this.articleDto)
        .subscribe({
          next: (res) => {
            // Optionnel : Tu peux ajouter un petit message de succès ou un toast ici
            console.log('Quantité d\'alerte mise à jour avec succès', res);
          },
          error: (err) => {
            // Gestion de l'erreur (affichage dans la console ou via un service d'alerte)
            console.error('Erreur lors de la mise à jour de la quantité d\'alerte', err);
          }
        });
    }
  }
}






