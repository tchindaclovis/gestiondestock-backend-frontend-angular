import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {ArticleDto, UtilisateurDto} from "../../../../gs-api/src";
import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";

@Component({
  selector: 'app-page-utilisateur',
  templateUrl: './page-utilisateur.component.html',
  styleUrls: ['./page-utilisateur.component.scss']
})
export class PageUtilisateurComponent implements OnInit {
  listUtilisateur: Array<UtilisateurDto> = [];
  errorMsg = '';
  constructor(
    private router: Router,
    private utilisateurService: UtilisateurService
  ) { }

  ngOnInit(): void {
    this.findListUtilisateur()
  }

  findListUtilisateur(): void{
    this.utilisateurService.findAllUtilisateur()
      .subscribe(utilisateurs =>{
        this.listUtilisateur = utilisateurs;
      });
  }


  nouvelUtilisateur(): void{
    this.router.navigate(['nouvelutilisateur']);
  }


  handleSuppression(event: any): void {
    if(event === 'success'){
      this.findListUtilisateur();
    } else {
      this.errorMsg = event;
    }
  }

}
