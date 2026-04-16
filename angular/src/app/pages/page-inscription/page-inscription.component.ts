import { Component, OnInit } from '@angular/core';
import {AdresseDto, AuthenticationRequest, EntrepriseDto} from "../../../gs-api/src";
import {EntrepriseService} from "../../services/entreprise/entreprise.service";
import {UserService} from "../../services/user/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-page-inscription',
  templateUrl: './page-inscription.component.html',
  styleUrls: ['./page-inscription.component.scss']
})
export class PageInscriptionComponent implements OnInit {

  entrepriseDto: EntrepriseDto = {};
  adresse: AdresseDto = {};
  errorMsg: Array<string> =[];
  errorMessage = '';

  constructor(
    private entrepriseService: EntrepriseService,
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  inscrire(): void {
    // On copie l'adresse dans l'objet entreprise avant l'envoi
    this.entrepriseDto.adresse = this.adresse;

    // Appel à l'API d'inscription
    this.entrepriseService.sinscrire(this.entrepriseDto)
      .subscribe({
        next: () => {
          // Une fois inscrit, on lance automatiquement la connexion
          this.connectEntreprise();
        },
        error: (error) => {
          // Affichage des erreurs d'inscription renvoyées par le backend
          this.errorMsg = error.error.errors;
        }
      });
  }

  connectEntreprise(): void {
    const authenticationRequest: AuthenticationRequest = {
      login: this.entrepriseDto.email!,
      password: 'som3R@nd0mP@$$word'
    };

    this.userService.login(authenticationRequest)
      .subscribe({
        next: async (response: any) => {

          // convert Blob -> JSON si nécessaire
          if (response instanceof Blob) {
            response = JSON.parse(await response.text());
          }

          // extraction token
          const token =
            typeof response.accessToken === 'string'
              ? response.accessToken
              : response.accessToken?.token;

          if (!token) {
            this.errorMessage = 'Erreur : aucun token reçu du serveur.';
            console.error("❌ Aucun token reçu du backend");
            return;
          }

          // stockage token
          this.userService.setAccessToken(token);

          // --- CHANGEMENT ICI ---
          // On attend d'avoir l'utilisateur AVANT de rediriger
          const email = authenticationRequest.login!;

          if (!email) {
            console.error("Email manquant pour la récupération de l'utilisateur.");
            return;
          }

          this.userService.getUserByEmail(email)
            .subscribe({
              next: user => {
                this.userService.setConnectedUser(user);
                localStorage.setItem('origin', 'inscription');
                this.router.navigate(['changermotdepasse']);
              },
              error: err => console.error("Erreur getUserByEmail :", err)
            });

        },

        error: err => {
          console.error("❌ Erreur pendant la connexion automatique :", err);
        }
      });
  }
}






// import { Component, OnInit } from '@angular/core';
// import {AdresseDto, AuthenticationRequest, EntrepriseDto} from "../../../gs-api/src";
// import {EntrepriseService} from "../../services/entreprise/entreprise.service";
// import {UserService} from "../../services/user/user.service";
// import {Router} from "@angular/router";
//
// @Component({
//   selector: 'app-page-inscription',
//   templateUrl: './page-inscription.component.html',
//   styleUrls: ['./page-inscription.component.scss']
// })
// export class PageInscriptionComponent implements OnInit {
//
//   entrepriseDto: EntrepriseDto = {};
//   adresse: AdresseDto = {};
//   errorsMsg: Array<string> =[];
//
//   constructor(
//     private entrepriseService: EntrepriseService,
//     private userService: UserService,
//     private router: Router
//   ) { }
//
//   ngOnInit(): void {
//   }
//
//   inscrire(): void {
//     // On copie l'adresse dans l'objet entreprise avant l'envoi
//     this.entrepriseDto.adresse = this.adresse;
//
//     // Appel à l'API d'inscription
//     this.entrepriseService.sinscrire(this.entrepriseDto)
//       .subscribe({
//         next: () => {
//           // Une fois inscrit, on lance automatiquement la connexion
//           this.connectEntreprise();
//         },
//         error: (error) => {
//           // Affichage des erreurs d'inscription renvoyées par le backend
//           this.errorsMsg = error.error.errors;
//         }
//       });
//   }
//
//
//   connectEntreprise(): void {
//
//     const authenticationRequest: AuthenticationRequest = {
//       login: this.entrepriseDto.email!,
//       password: 'som3R@nd0mP@$$word'
//     };
//
//     this.userService.login(authenticationRequest)
//       .subscribe({
//         next: async (response: any) => {
//
//           // ---- (1) Conversion Blob -> JSON ----
//           if (response instanceof Blob) {
//             const text = await response.text();
//             try {
//               response = JSON.parse(text);
//             } catch (e) {
//               console.error("❌ Impossible de parser le blob comme JSON :", e);
//               return;
//             }
//           }
//
//           // ---- (2) Extraction correcte du token ----
//           const token =
//             typeof response.accessToken === 'string'
//               ? response.accessToken
//               : response.accessToken?.token;
//
//           if (!token) {
//             console.error("❌ Aucun token reçu du backend");
//             return;
//           }
//
//           // ---- (3) Stockage du token ----
//           this.userService.setAccessToken(token);
//           // ---- (4) Récupération + enregistrement utilisateur ----
//           this.getUserByEmail(authenticationRequest.login)
//           localStorage.setItem('origin', 'inscription');
//           this.router.navigate(['changermotdepasse']);
//
//
//           // // ---- (4) Récupération + enregistrement utilisateur ----
//           // this.userService.getUserByEmail(authenticationRequest.login)
//           //   .subscribe({
//           //     next: user => {
//           //       this.userService.setConnectedUser(user);
//           //
//           //       // ---- (5) Redirection finale ----
//           //       this.router.navigate(['changermotdepasse']);
//           //     },
//           //
//           //     error: err => {
//           //       console.error("❌ Impossible de charger l'utilisateur après login :", err);
//           //     }
//           //   });
//         },
//
//         error: err => {
//           console.error("❌ Erreur pendant la connexion automatique :", err);
//         }
//       });
//   }
//
//   getUserByEmail(email?: string): void {
//     this.userService.getUserByEmail(email)
//       .subscribe(user => {
//         this.userService.setConnectedUser(user);
//       });
//   }
//
// }









// import { Component, OnInit } from '@angular/core';
// import {AdresseDto, AuthenticationRequest, EntrepriseDto} from "../../../gs-api/src";
// import {EntrepriseService} from "../../services/entreprise/entreprise.service";
// import {UserService} from "../../services/user/user.service";
// import {Router} from "@angular/router";
//
// @Component({
//   selector: 'app-page-inscription',
//   templateUrl: './page-inscription.component.html',
//   styleUrls: ['./page-inscription.component.scss']
// })
// export class PageInscriptionComponent implements OnInit {
//
//   entrepriseDto: EntrepriseDto = {};
//   adresse: AdresseDto = {};
//   errorsMsg: Array<string> =[];
//
//   constructor(
//     private entrepriseService: EntrepriseService,
//     private userService: UserService,
//     private router: Router
//   ) { }
//
//   ngOnInit(): void {
//   }
//
//   inscrire(): void{
//     this.entrepriseDto.adresse = this.adresse;
//     this.entrepriseService.sinscrire(this.entrepriseDto)
//       .subscribe(entrepriseDto => {
//         this.connectEntreprise();
//       }, error => {
//         this.errorsMsg = error.error.errors;
//       });
//   }
//
//
//   // Dans la méthode connectEntreprise :
//
//   connectEntreprise(): void{
//     const authenticationRequest: AuthenticationRequest = {
//       login: this.entrepriseDto.email,
//       password: 'som3R@nd0mP@$$word'
//     };
//     this.userService.login(authenticationRequest)
//     .subscribe(response => {
//       // CORRECTION ICI : Accéder à la propriété accessToken
//       const token = response.accessToken;
//       if (token) {
//         this.userService.setAccessToken(token); // On passe la chaîne (string)
//         this.getUserByEmail(authenticationRequest.login);
//         localStorage.setItem('origin', 'inscription');
//         this.router.navigate(['changermotdepasse']);
//       } else {
//         // Gérer le cas où le token est manquant dans la réponse
//         console.error('Token non reçu après inscription/connexion automatique');
//       }
//     });
//   }
//
//
//   // connectEntreprise(): void {
//   //   const authenticationRequest: AuthenticationRequest = {
//   //     login: this.entrepriseDto.email,
//   //     password: 'som3R@nd0mP@$$word'
//   //   };
//   //
//   //   this.userService.login(authenticationRequest).subscribe({
//   //     next: (response) => {
//   //
//   //       const token = response?.accessToken;
//   //       if (!token) {
//   //         this.errorsMsg = ['Erreur : token non trouvé dans la réponse'];
//   //         return;
//   //       }
//   //
//   //       this.userService.setAccessToken(token);
//   //
//   //       this.getUserByEmail(authenticationRequest.login);
//   //       localStorage.setItem('origin', 'inscription');
//   //       this.router.navigate(['changermotdepasse']);
//   //     },
//   //     error: () => {
//   //       this.errorsMsg = ['Erreur connexion auto après inscription'];
//   //     }
//   //   });
//   // }
//
//
//   // connectEntreprise(): void{
//   //   const authenticationRequest: AuthenticationRequest = {
//   //     login: this.entrepriseDto.email,
//   //     password: 'som3R@nd0mP@$$word'
//   //   };
//   //   this.userService.login(authenticationRequest)
//   //     .subscribe(response => {
//   //       this.userService.setAccessToken(response);
//   //       this.getUserByEmail(authenticationRequest.login);
//   //       localStorage.setItem('origin', 'inscription');
//   //       this.router.navigate(['changermotdepasse']);
//   //     });
//   // }
//
//   getUserByEmail(email?: string): void{
//     this.userService.getUserByEmail(email)
//       .subscribe(user => {
//         this.userService.setConnectedUser(user);
//       });
//   }
//
// }




// import { Component } from '@angular/core';
// import { AdresseDto, AuthenticationRequest, EntrepriseDto } from '../../../gs-api/src';
// import { EntrepriseService } from '../../services/entreprise/entreprise.service';
// import { UserService } from '../../services/user/user.service';
// import { Router } from '@angular/router';
//
// @Component({
//   selector: 'app-page-inscription',
//   templateUrl: './page-inscription.component.html',
//   styleUrls: ['./page-inscription.component.scss']
// })
// export class PageInscriptionComponent {
//
//   entreprise: EntrepriseDto = { adresse: {} };
//   errorsMsg: string[] = [];
//
//   constructor(
//     private entrepriseService: EntrepriseService,
//     private userService: UserService,
//     private router: Router
//   ) {}
//
//     ngOnInit(): void {
//     }
//
//   inscrire(): void {
//     this.entrepriseService.sinscrire(this.entreprise).subscribe({
//       next: () => this.loginAfterRegister(),
//       error: (err) => {
//         this.errorsMsg = err.error?.errors || ['Erreur inconnue'];
//       }
//     });
//   }
//
//   private loginAfterRegister(): void {
//     const request: AuthenticationRequest = {
//       login: this.entreprise.email,
//       password: 'som3R@nd0mP@$$word'
//     };
//
//     this.userService.login(request).subscribe(res => {
//       this.userService.setAccessToken(res);
//       this.userService.getUserByEmail(request.login).subscribe(user => {
//         this.userService.setConnectedUser(user);
//         localStorage.setItem('origin', 'inscription');
//         this.router.navigate(['changermotdepasse']);
//       });
//     });
//   }
// }




// import { Component } from '@angular/core';
// import { AdresseDto, AuthenticationRequest, EntrepriseDto } from '../../../gs-api/src';
// import { EntrepriseService } from '../../services/entreprise/entreprise.service';
// import { UserService } from '../../services/user/user.service';
// import { Router } from '@angular/router';
//
// @Component({
//   selector: 'app-page-inscription',
//   templateUrl: './page-inscription.component.html',
//   styleUrls: ['./page-inscription.component.scss']
// })
// export class PageInscriptionComponent {
//
//   // 👉 Propriétés attendues dans le HTML
//   entrepriseDto: EntrepriseDto = {
//     nom: '',
//     email: '',
//     numTel: '',
//     description: '',
//     codeFiscal: '',
//     adresse: {}
//   };
//
//   adresse: AdresseDto = {
//     adresse1: '',
//     adresse2: '',
//     ville: '',
//     codePostale: '',
//     pays: ''
//   };
//
//   errorsMsg: string[] = [];
//
//   constructor(
//     private entrepriseService: EntrepriseService,
//     private userService: UserService,
//     private router: Router
//   ) {}
//
//   inscrire(): void {
//     this.entrepriseDto.adresse = this.adresse;
//
//     this.entrepriseService.sinscrire(this.entrepriseDto)
//       .subscribe({
//       next: () => this.loginAfterRegister(),
//       error: (err) => {
//         this.errorsMsg = err.error?.errors || ['Erreur inconnue'];
//       }
//     });
//   }
//
//   private loginAfterRegister(): void {
//     const request: AuthenticationRequest = {
//       login: this.entrepriseDto.email,
//       password: 'som3R@nd0mP@$$word'
//     };
//
//     this.userService.login(request).subscribe(res => {
//       this.userService.setAccessToken(res);
//       this.userService.getUserByEmail(request.login).subscribe(user => {
//         this.userService.setConnectedUser(user);
//         localStorage.setItem('origin', 'inscription');
//         this.router.navigate(['changermotdepasse']);
//       });
//     });
//   }
// }
