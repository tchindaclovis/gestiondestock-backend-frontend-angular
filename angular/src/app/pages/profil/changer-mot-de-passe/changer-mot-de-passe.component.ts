import {Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import { ChangerMotDePasseUtilisateurDto } from '../../../../gs-api/src';
import { UserService } from '../../../services/user/user.service';

@Component({
  selector: 'app-changer-mot-de-passe',
  templateUrl: './changer-mot-de-passe.component.html',
  styleUrls: ['./changer-mot-de-passe.component.scss']
})
export class ChangerMotDePasseComponent implements OnInit {

  changerMotDePasseDto: ChangerMotDePasseUtilisateurDto = {};
  ancienMotDePasse = '';

  constructor(
    private router: Router,
    private userService: UserService
  ) { }

    ngOnInit(): void {
      if (localStorage.getItem('origin') && localStorage.getItem('origin') === 'inscription') {
      this.ancienMotDePasse = 'som3R@nd0mP@$$word';
      localStorage.removeItem('origin');
       }
    }

  cancelClick(): void {
    this.router.navigate(['profil']);
  }

  changerMotDePasseUtilisateur(): void {
    const user = this.userService.getConnectedUser();
    if (!user) return;

    this.changerMotDePasseDto.id = user.id;

    this.userService.changerMotDePasse(this.changerMotDePasseDto)
      .subscribe({
      next: () => this.router.navigate(['profil'])
    });
  }
}






// import {Component, OnInit} from '@angular/core';
// import { Router } from '@angular/router';
// import {CategoriesService, ChangerMotDePasseUtilisateurDto} from '../../../../gs-api/src';
// import { UserService } from '../../../services/user/user.service';
//
// @Component({
//   selector: 'app-changer-mot-de-passe',
//   templateUrl: './changer-mot-de-passe.component.html',
//   styleUrls: ['./changer-mot-de-passe.component.scss']
// })
// export class ChangerMotDePasseComponent implements OnInit{
//
//   constructor(
//     private utili: CategoriesService,
//     private router: Router
//   ) {}
//
//
//   ngOnInit(): void {
//       this.utili.findAll7()
//       .subscribe(res => {
//     });
//   }
//
//   cancel() : void{
//     this.router.navigate(['profil']);
//   }
// }



// import { Component } from '@angular/core';
// import { Router } from '@angular/router';
// import { ChangerMotDePasseUtilisateurDto } from '../../../../gs-api/src';
// import { UserService } from '../../../services/user/user.service';
//
// @Component({
//   selector: 'app-changer-mot-de-passe',
//   templateUrl: './changer-mot-de-passe.component.html',
//   styleUrls: ['./changer-mot-de-passe.component.scss']
// })
// export class ChangerMotDePasseComponent {
//
//   dto: ChangerMotDePasseUtilisateurDto = {};
//   ancienMotDePasse = '';
//
//   constructor(
//     private router: Router,
//     private userService: UserService
//   ) {
//     if (localStorage.getItem('origin') === 'inscription') {
//       this.ancienMotDePasse = 'som3R@nd0mP@$$word';
//       localStorage.removeItem('origin');
//     }
//   }
//
//   submit() {
//     const user = this.userService.getConnectedUser();
//     if (!user) return;
//
//     this.dto.id = user.id;
//
//     this.userService.changerMotDePasse(this.dto).subscribe({
//       next: () => this.router.navigate(['profil'])
//     });
//   }
// }








// import { Component, OnInit } from '@angular/core';
// import {Router} from "@angular/router";
// import {ChangerMotDePasseUtilisateurDto} from "../../../../gs-api/src";
// import {UserService} from "../../../services/user/user.service";
//
// @Component({
//   selector: 'app-changer-mot-de-passe',
//   templateUrl: './changer-mot-de-passe.component.html',
//   styleUrls: ['./changer-mot-de-passe.component.scss']
// })
// export class ChangerMotDePasseComponent implements OnInit {
//
//   changerMotDePasseDto: ChangerMotDePasseUtilisateurDto = {};
//   ancienMotDePasse = '';
//
//   constructor(
//     private router: Router,
//     private userService: UserService
//   ) { }
//
//   ngOnInit(): void {
//     if (localStorage.getItem('origin') && localStorage.getItem('origin') === 'inscription') {
//       this.ancienMotDePasse = 'som3R@nd0mP@$$word';
//       localStorage.removeItem('origin');
//     }
//   }
//
//   cancelClick(): void{
//     this.router.navigate(['profil'])
//   }
//
//   changerMotDePasseUtilisateur(): void {
//     // @ts-ignore
//     this.changerMotDePasseDto.id = this.userService.getConnectedUser().id;
//     this.userService.changerMotDePasse(this.changerMotDePasseDto)
//       .subscribe(data => {
//         // rien faire
//         this.router.navigate(['profil']);
//       });
//   }
// }
