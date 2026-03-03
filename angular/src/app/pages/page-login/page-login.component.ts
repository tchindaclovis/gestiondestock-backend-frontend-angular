import { Component, OnInit } from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {AuthenticationRequest} from '../../../gs-api/src/model/authenticationRequest';
import {Router} from '@angular/router';

@Component({
  selector: 'app-page-login',
  templateUrl: './page-login.component.html',
  styleUrls: ['./page-login.component.scss']
})
export class PageLoginComponent implements OnInit {

  authenticationRequest: AuthenticationRequest = {};
  errorMessage = '';

  constructor(
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  login() {

    this.userService.login(this.authenticationRequest).subscribe(
      async (data: any) => {

        if (data instanceof Blob) {
          data = JSON.parse(await data.text());
        }

        const token =
          typeof data.accessToken === 'string'
            ? data.accessToken
            : data.accessToken?.token;

        if (!token) {
          this.errorMessage = 'Erreur : aucun token reçu du serveur.';
          return;
        }

        // stockage token
        this.userService.setAccessToken(token);

        // récupération utilisateur
        this.userService.getUserByEmail(this.authenticationRequest.login!)
          .subscribe({
            next: user => {
              this.userService.setConnectedUser(user);
              this.router.navigate(['']);
            },
            error: () => {
              this.errorMessage = "Impossible de charger l'utilisateur.";
            }
          });
      },

      error => {
        this.errorMessage = 'Login et / ou mot de passe incorrect';
      });
  }
}







// import { Component, OnInit } from '@angular/core';
// import {UserService} from '../../services/user/user.service';
// import {AuthenticationRequest} from '../../../gs-api/src/model/authenticationRequest';
// import {Router} from '@angular/router';
//
// @Component({
//   selector: 'app-page-login',
//   templateUrl: './page-login.component.html',
//   styleUrls: ['./page-login.component.scss']
// })
// export class PageLoginComponent implements OnInit {
//
//   authenticationRequest: AuthenticationRequest = {};
//   errorMessage = '';
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) { }
//
//   ngOnInit(): void {
//   }
//
//   login() {
//     console.log('Tentative de connexion avec :', this.authenticationRequest);
//
//     this.userService.login(this.authenticationRequest).subscribe(
//       async (data: any) => {
//
//         console.log('✅ Réponse reçue du backend :', data);
//
//         // Si c'est un Blob (cas OpenAPI), on le convertit en JSON
//         if (data instanceof Blob) {
//           const text = await data.text(); // lit le contenu du blob
//           data = JSON.parse(text);        // convertit en JSON
//         }
//
//
//         // Vérifie la présence du token JWT
//         if (data && data.accessToken) {
//           console.log("TYPE accessToken =", typeof data.accessToken);
//           console.log('🎟️ Token JWT :', data.accessToken);
//
//           // Détermine la chaîne réelle du token
//           const realToken =
//             typeof data.accessToken === 'string'
//               ? data.accessToken
//               : data.accessToken.token;
//
//           // ---- (3) Sauvegarde du token JWT ----
//           // Utilise la méthode du service qui stocke la chaîne du token proprement
//           this.userService.setAccessToken(realToken);
//
//           // ---- (4) Récupération + enregistrement utilisateur ----
//           this.userService.getUserByEmail(this.authenticationRequest.login)
//             .subscribe({
//               next: user => {
//                 this.userService.setConnectedUser(user);
//
//                 // ---- (5) Redirection finale ----
//                 this.router.navigate(['']);
//               },
//
//               error: err => {
//                 console.error("❌ Impossible de charger l'utilisateur après login :", err);
//               }
//             });
//
//         } else {
//           this.errorMessage = 'Erreur : aucun token reçu du serveur.';
//         }
//       },
//       (error) => {
//         console.log('Erreur reçue depuis le backend :', error);
//         this.errorMessage = 'Login et / ou mot de passe incorrect';
//       });
//   }
// }







// import { Component, OnInit } from '@angular/core';
// import {UserService} from '../../services/user/user.service';
// import {AuthenticationRequest} from '../../../gs-api/src/model/authenticationRequest';
// import {Router} from '@angular/router';
// //import {debug} from 'ng-packagr/lib/util/log';
//
// @Component({
//   selector: 'app-page-login',
//   templateUrl: './page-login.component.html',
//   styleUrls: ['./page-login.component.scss']
// })
// export class PageLoginComponent implements OnInit {
//
//   authenticationRequest: AuthenticationRequest = {};
//   errorMessage = '';
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) { }
//
//   ngOnInit(): void {
//   }
//
//   // tslint:disable-next-line:typedef
//
//   login() {
//     this.userService.login(this.authenticationRequest).subscribe({
//       next: (data) => {
//         const token = data?.accessToken;   // <-- C'est CORRECT !
//         if (!token) {
//           this.errorMessage = 'Token invalide reçu du serveur';
//           return;
//         }
//         this.userService.setAccessToken(token); // Passe le string
//         this.userService.getUserByEmail(this.authenticationRequest.login!)
//           .subscribe(user => {
//             this.userService.setConnectedUser(user);
//             this.router.navigate(['']);
//           });
//       },
//       error: () => {
//         this.errorMessage = 'Login ou mot de passe incorrect';
//       }
//     });
//   }
//
//   getUserByEmail(): void {
//     this.userService.getUserByEmail(this.authenticationRequest.login)
//       .subscribe(user => {
//         this.userService.setConnectedUser(user);
//       });
//   }
//
// }


// login() {
//   this.userService.login(this.authenticationRequest).subscribe({
//     next: (response) => {
//
//       const token = response.accessToken;
//       this.userService.setAccessToken(token);
//       this.userService.getUserByEmail(this.authenticationRequest.login)
//         .subscribe(user => {
//           this.userService.setConnectedUser(user);
//           this.router.navigate(['']);
//         });
//     },
//     error: () => {
//       this.errorMessage = 'Login ou mot de passe incorrect';
//     }
//   });
// }

// login() {
//   this.userService.login(this.authenticationRequest).subscribe((data) => {
//     this.userService.setAccessToken(data);
//     this.getUserByEmail();
//     this.router.navigate(['']);
//   }, error => {
//     this.errorMessage = 'Login et / ou mot de passe incorrecte';
//   });
// }





// import { Component } from '@angular/core';
// import { Router } from '@angular/router';
// import { AuthenticationRequest } from '../../../gs-api/src/model/authenticationRequest';
// import { UserService } from '../../services/user/user.service';
//
// @Component({
//   selector: 'app-page-login',
//   templateUrl: './page-login.component.html',
//   styleUrls: ['./page-login.component.scss']
// })
// export class PageLoginComponent {
//
//   authenticationRequest: AuthenticationRequest = {};
//   errorMessage = '';
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) {}
//
//   login() {
//     this.userService.login(this.authenticationRequest).subscribe({
//       next: (data) => {
//         this.userService.setAccessToken(data);
//         this.userService.getUserByEmail(this.authenticationRequest.login)
//           .subscribe(user => {
//             this.userService.setConnectedUser(user);
//             this.router.navigate(['']);
//           });
//       }, error: () => {
//         this.errorMessage = 'Login ou mot de passe incorrect'
//       }
//     });
//   }
//
//   getUserByEmail(): void {
//   this.userService.getUserByEmail(this.authenticationRequest.login)
//     .subscribe(user => {
//       this.userService.setConnectedUser(user);
//     });
//   }
// }








// import { Component } from '@angular/core';
// import { Router } from '@angular/router';
// import { AuthenticationRequest } from '../../../gs-api/src/model/authenticationRequest';
// import { UserService } from '../../services/user/user.service';
//
// @Component({
//   selector: 'app-page-login',
//   templateUrl: './page-login.component.html',
//   styleUrls: ['./page-login.component.scss']
// })
// export class PageLoginComponent {
//
//   req: AuthenticationRequest = {};
//   error = '';
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) {}
//
//   login() {
//     this.userService.login(this.req).subscribe({
//       next: (res) => {
//         this.userService.setAccessToken(res);
//         this.userService.getUserByEmail(this.req.login).subscribe(user => {
//           this.userService.setConnectedUser(user);
//           this.router.navigate(['']);
//         });
//       },
//       error: () => this.error = 'Login ou mot de passe incorrect.'
//     });
//   }
// }





// import { Component, OnInit } from '@angular/core';
// import {UserService} from "../../services/user/user.service";
// import {AuthenticationRequest} from '../../../gs-api/src/model/authenticationRequest';
// import {Router} from "@angular/router";
//
// @Component({
//   selector: 'app-page-login',
//   templateUrl: './page-login.component.html',
//   styleUrls: ['./page-login.component.scss']
// })
// export class PageLoginComponent implements OnInit {
//
//   authenticationRequest: AuthenticationRequest = {};
//   errorMessage = '';
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) { }
//
//   ngOnInit(): void {
//   }
//
//   login() {
//     console.log('Tentative de connexion avec :', this.authenticationRequest);
//
//     this.userService.login(this.authenticationRequest).subscribe(
//       async (data: any) => {
//         console.log('✅ Réponse reçue du backend :', data);
//
//         // Si c'est un Blob (cas OpenAPI), on le convertit en JSON
//         if (data instanceof Blob) {
//           const text = await data.text(); // lit le contenu du blob
//           data = JSON.parse(text);        // convertit en JSON
//         }
//
//         // Vérifie que le token existe
//         if (data && data.accessToken) {
//           console.log('🎟️ Token JWT :', data.accessToken);
//           this.userService.setAccessToken(data);
//           this.getUserByEmail();
//           this.router.navigate(['']);  // ou '/dashboard' selon ta route
//         } else {
//           this.errorMessage = 'Erreur : aucun token reçu du serveur.';
//         }
//       },
//       (error) => {
//         console.log('Erreur reçue depuis le backend :', error);
//         this.errorMessage = 'Login et / ou mot de passe incorrect';
//       });
//   }
//
//   getUserByEmail(): void{
//     this.userService.getUserByEmail(this.authenticationRequest.login)
//       .subscribe(user => {
//         this.userService.setConnectedUser(user);
//       });
//   }
//
// }


// import { Component, OnInit } from '@angular/core';
// import { UserService } from "../../services/user/user.service";
// import { AuthenticationRequest } from '../../../gs-api/src/model/authenticationRequest';
// import { Router } from "@angular/router";
//
// @Component({
//   selector: 'app-page-login',
//   templateUrl: './page-login.component.html',
//   styleUrls: ['./page-login.component.scss']
// })
// export class PageLoginComponent implements OnInit {
//
//   authenticationRequest: AuthenticationRequest = {};
//   errorMessage = '';
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) {}
//
//   ngOnInit(): void {}
//
//   login(): void {
//     console.log('Tentative de connexion avec :', this.authenticationRequest);
//
//     if (!this.authenticationRequest.login || !this.authenticationRequest.password) {
//       this.errorMessage = 'Veuillez saisir votre email et mot de passe.';
//       return;
//     }
//
//     this.userService.login(this.authenticationRequest).subscribe({
//       next: async (response: any) => {
//         console.log('✅ Réponse reçue du backend :', response);
//
//         let data: any = response;
//
//         // Si la réponse est un Blob (cas fréquent avec Swagger/OpenAPI)
//         if (response instanceof Blob) {
//           try {
//             const text = await response.text();
//             data = JSON.parse(text);
//           } catch (err) {
//             console.error('Erreur lors du parsing du Blob :', err);
//             this.errorMessage = 'Erreur de communication avec le serveur.';
//             return;
//           }
//         }
//
//         // Vérifie la présence du token JWT
//         if (data && data.accessToken) {
//           console.log('🎟️ Token JWT reçu :', data.accessToken);
//
//           // Sauvegarde du token
//           this.userService.setAccessToken(data);
//
//           // Récupération des infos utilisateur
//           this.getUserByEmail();
//
//           // Redirection
//           this.router.navigate(['/dashboard']); // change selon ta route
//         } else {
//           this.errorMessage = 'Erreur : aucun token reçu du serveur.';
//         }
//       },
//       error: (err) => {
//         console.error('Erreur reçue depuis le backend :', err);
//         this.errorMessage = 'Login et / ou mot de passe incorrect';
//       }
//     });
//   }
//
//   private getUserByEmail(): void {
//     if (!this.authenticationRequest.login) {
//       console.warn('Aucun email fourni pour la récupération utilisateur.');
//       return;
//     }
//
//     this.userService.getUserByEmail(this.authenticationRequest.login).subscribe({
//       next: (user) => {
//         this.userService.setConnectedUser(user);
//       },
//       error: (err) => {
//         console.error('Erreur lors de la récupération de l’utilisateur :', err);
//       }
//     });
//   }
// }














