import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {ArticleDto, ArticlesService, CategoryDto} from "../../../gs-api/src";
import {Observable, of} from "rxjs";
import {ActivatedRoute} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(
    private userService: UserService,
    private articlesService: ArticlesService
  ) { }


  enregistrerArticle(articleDto: ArticleDto): Observable<ArticleDto>{
    articleDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.articlesService.save8(articleDto);
  }

  findAllArticle(): Observable<ArticleDto[]>{  //renvoit un observable de listes d'articleDto
    return this.articlesService.findAll8();
  }

  findArticleById(idArticle?: number): Observable<ArticleDto>{  //renvoit un observable d'articleDto
    if (idArticle){   //à cause du poin d'interrogation on est obligé de faire cette vérification
      return this.articlesService.findById8(idArticle);
    }
    return of();
  }

  deleteArticle(idArticle: number): Observable<any> { //type de retour est un observable de any
    if(idArticle){
      this.articlesService.delete8(idArticle);
    }
    return of();  //sinon retourne un observable vide
  }

  findArticleByCode(codeArticle: string): Observable<ArticleDto> {
    return this.articlesService.findByCodeArticle(codeArticle);
  }
}
