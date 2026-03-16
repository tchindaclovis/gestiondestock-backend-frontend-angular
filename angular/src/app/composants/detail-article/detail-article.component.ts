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


  confirmerEtSupprimerArt(): void {
    if (this.articleDto.id){
     this.articleService.deleteArticle(this.articleDto.id)
       .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
         this.suppressionResult.emit('success');
       }, error => {
         this.suppressionResult.emit(error.error.error);
       });
    }
  }

  appercuArticle(): void {
    this.router.navigate(['appercuarticle', this.articleDto.id]);
  }
}
