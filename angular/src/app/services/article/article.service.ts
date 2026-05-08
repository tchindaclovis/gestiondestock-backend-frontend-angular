import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {ArticleDto, ArticlesService, CategoryDto} from "../../../gs-api/src";
import {Observable, of} from "rxjs";
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  private baseUrl: any;

  constructor(
    private userService: UserService,
    private articlesService: ArticlesService, // Service généré par OpenAPI
    private http: HttpClient                  // <--- Vérifie bien le "private" ici
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
      return this.articlesService.delete8(idArticle);
    }
    return of();  //sinon retourne un observable vide
  }

  findArticleByCode(codeArticle: string): Observable<ArticleDto> {
    return this.articlesService.findByCodeArticle(codeArticle);
  }

  findAllArticleByIdCategory(idCategory: number): Observable<ArticleDto[]> {
    if (idCategory) {
      return this.articlesService.findAllArticleByIdCategory(idCategory);
    }
    return of([]);
  }

  findAllArticlesByIdEntreprise(idEntreprise: number): Observable<ArticleDto[]> {
    if (idEntreprise) {
      return this.articlesService.findAllArticleByIdEntreprise(idEntreprise);
    }
    return of([]);
  }

  // getLastCodeArticle(): Observable<string> {
  //   return this.articlesService.getLastCodeArticle();
  // }

  /**
   * Correction : On court-circuite le service généré pour cet appel spécifique
   * afin de spécifier que la réponse est du TEXTE pur et non du JSON.
   */
  getLastCodeArticle(): Observable<string> {
    // On récupère l'URL de base depuis le service généré ou on la définit
    const url = 'http://localhost:8081/gestiondestock/v1/articles/lastcodearticle';

    // L'option { responseType: 'text' } est CRUCIALRE ici
    return this.http.get(url, { responseType: 'text' });
  }
}
