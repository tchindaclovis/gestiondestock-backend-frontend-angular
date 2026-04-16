import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ArticleDto, CategoryDto} from "../../../gs-api/src";
import {Router} from "@angular/router";
import {ArticleService} from "../../services/article/article.service";

@Component({
  selector: 'app-detail-article',
  templateUrl: './detail-article.component.html',
  styleUrls: ['./detail-article.component.scss']
})
export class DetailArticleComponent implements OnInit {

  @Input()
  articleDto: ArticleDto = {};

  @Output()
  suppressionResult = new EventEmitter();

  constructor(
    private router: Router,
    private articleService: ArticleService
  ) { }

  ngOnInit(): void {
  }


  modifierArticle(): void {
    this.router.navigate(['nouvelarticle', this.articleDto.id]);
  }


  // confirmerEtSupprimerArt(): void {
  //   if (this.articleDto.id){
  //    this.articleService.deleteArticle(this.articleDto.id)
  //      .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
  //        this.suppressionResult.emit('success');
  //      }, error => {
  //        this.suppressionResult.emit(error.error.error);
  //      });
  //   }
  // }


  // confirmerEtSupprimerArt(): void {
  //   if (this.articleDto.id) {
  //     this.articleService.deleteArticle(this.articleDto.id)
  //       .subscribe({
  //         next: () => {
  //           // On notifie le parent (page-article) pour rafraîchir la liste
  //           this.suppressionResult.emit('success');
  //         },
  //         error: (err) => {
  //           // On extrait le message d'erreur du backend
  //           const msg = err.error?.message || "Une erreur est survenue lors de la suppression";
  //           this.suppressionResult.emit(msg);
  //         }
  //       });
  //   }
  // }


  confirmerEtSupprimerArt(): void {
    console.log('Tentative de suppression de l\'ID :', this.articleDto.id); // LOG 1
    if (this.articleDto.id) {
      this.articleService.deleteArticle(this.articleDto.id)
        .subscribe({
          next: (res) => {
            console.log('Suppression réussie côté Backend'); // LOG 2
            this.suppressionResult.emit('success');
          },
          error: (err) => {
            console.error('Erreur reçue du Backend :', err); // LOG 3
            this.suppressionResult.emit(err);
          }
        });
    } else {
      console.error('Erreur : ID de l\'article est introuvable');
    }
  }

  appercuArticle(): void {
    this.router.navigate(['appercuarticle', this.articleDto.id]);
  }
}
