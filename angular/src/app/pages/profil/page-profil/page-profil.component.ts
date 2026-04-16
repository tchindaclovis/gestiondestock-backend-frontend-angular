import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {PhotoService, UtilisateurDto} from "../../../../gs-api/src";
import {UserService} from "../../../services/user/user.service";

@Component({
  selector: 'app-page-profil',
  templateUrl: './page-profil.component.html',
  styleUrls: ['./page-profil.component.scss']
})
export class PageProfilComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;
  imgUrl: string | ArrayBuffer = 'assets/profil.png';

  constructor(
    private router: Router,
  private userService: UserService,
  private photoService: PhotoService,
  ) { }

  ngOnInit(): void {
    this.connectedUser = this.userService.getConnectedUser();
  }

  modifierMotDePasse(): void{
    this.router.navigate(['changermotdepasse'])
  }

  modifierProfil() {
    this.router.navigate(['nouvelutilisateur', this.connectedUser?.id]);
  }
}
