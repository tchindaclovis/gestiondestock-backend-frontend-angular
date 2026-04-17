import {Component, Input, OnInit, Output, EventEmitter, SimpleChanges} from '@angular/core';
import { ArticleDto, MvtStockDto } from '../../../gs-api/src';
import { MvtstockService } from '../../services/mvtstock/mvtstock.service';
import {Router} from "@angular/router";

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
    typeMvt: 'CORRECTION_POS' // Valeur par défaut
  };

  // @Output() suppressionResult = new EventEmitter<string>();

  // Émetteur pour demander au parent de rafraîchir la liste/stock après correction
  @Output() correctionStockEvent = new EventEmitter<string>();

  constructor(
    private router: Router,
    private mvtstockService: MvtstockService, // Injectez le service de mouvements

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

    const call = this.mvtStockDto.typeMvt === 'CORRECTION_POS'
      ? this.mvtstockService.correctionStockPos(this.mvtStockDto)
      : this.mvtstockService.correctionStockNeg(this.mvtStockDto);

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
  //   } else {
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
  //   }
  // }

  private handleSuccess(): void {
    // Réinitialisation du formulaire
    this.mvtStockDto = {
      quantite: 0,
      typeMvt: 'CORRECTION_POS'
    };
  }

}



