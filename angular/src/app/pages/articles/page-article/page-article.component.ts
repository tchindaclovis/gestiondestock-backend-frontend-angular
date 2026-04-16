import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {ArticleDto, UtilisateurDto} from "../../../../gs-api/src";
import {ArticleService} from "../../../services/article/article.service";
import {UserService} from "../../../services/user/user.service";

@Component({
  selector: 'app-page-article',
  templateUrl: './page-article.component.html',
  styleUrls: ['./page-article.component.scss']
})
export class PageArticleComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;
  listArticle: Array<ArticleDto> = [];
  errorMsg = '';

  constructor(
    private router: Router,
    private userService: UserService,
    private articleService: ArticleService
  ) {}

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();

    // 2. ENSUITE ON APPELLE LA MÉTHODE
    this.findListArticle();
  }


  findListArticle(): void {
    // On récupère l'id de l'entreprise de l'utilisateur connecté
    const idEntreprise = this.connectedUser?.entreprise?.id;

    if (idEntreprise) {
      this.articleService.findAllArticlesByIdEntreprise(idEntreprise)
        .subscribe(articles => {
          this.listArticle = articles;
        });
    }
  }

  // findListArticle(): void{
  //   this.articleService.findAllArticle()
  //     .subscribe(articles =>{
  //       this.listArticle = articles;
  //     });
  // }


  nouvelArticle(): void{
  this.router.navigate(['nouvelarticle']);
  }

  handleSuppression(event: any): void {
    if(event === 'success'){
      this.findListArticle();
    } else {
      this.errorMsg = event;
    }
  }
}
