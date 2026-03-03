import { Injectable } from '@angular/core';
import {Observable, of, switchMap, throwError} from 'rxjs';
import {
  AuthenticationsService,
  ChangerMotDePasseUtilisateurDto,
  UtilisateurDto,
  UtilisateursService
} from '../../../gs-api/src';
import { AuthenticationRequest } from '../../../gs-api/src/model/authenticationRequest';
import { AuthenticationResponse } from '../../../gs-api/src/model/authenticationResponse';
import { Router } from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(
    private authenticationsService: AuthenticationsService,
    private utilisateursService: UtilisateursService,
    private router: Router
  ) {}

  /** AUTHENTICATION */
  login(authenticationRequest: AuthenticationRequest):
    Observable<AuthenticationResponse> {
    return this.authenticationsService.authenticate(authenticationRequest);
  }

  /** GET USER BY EMAIL */

  getUserByEmail(email: string): Observable<UtilisateurDto> {
    if (!email) {
      return throwError(() => new Error('Email manquant'));
    }

    return this.utilisateursService.findByEmail(email).pipe(
      switchMap(async (response) => {
        // Si la réponse est un Blob, on le convertit en texte puis parse en JSON
        if (response instanceof Blob) {
          const text = await response.text();
          return JSON.parse(text);
        }
        // Sinon, on retourne la réponse telle quelle
        return response;
      })
    );
  }


  /** TOKEN STORAGE (CORRIGÉ) */
  setAccessToken(token: string): void {
    const tokenObj = { accessToken: token };
    localStorage.setItem('accessToken', JSON.stringify(tokenObj));
    console.log("accessToken :", tokenObj);
  }

  getAccessToken(): string | null {
    const raw = localStorage.getItem('accessToken');
    if (!raw) return null;

    try {
      const parsed = JSON.parse(raw);
      return parsed.accessToken ?? null;
    } catch {
      return null;
    }
  }

  /** CONNECTED USER STORAGE (CORRIGÉ) */
  setConnectedUser(utilisateur: UtilisateurDto): void {
    localStorage.setItem('connectedUser', JSON.stringify(utilisateur));
    console.log("Utilisateur chargé :", utilisateur);
  }


  getConnectedUser(): UtilisateurDto | null {
    const userJson = localStorage.getItem('connectedUser');
    if (!userJson) {
      return null;
    }

    try {
      return JSON.parse(userJson);
    } catch (e) {
      console.error("Erreur parsing connectedUser :", e);
      return null;
    }
  }


  /** CHANGE PASSWORD */
  changerMotDePasse(changerMotDePasseDto: ChangerMotDePasseUtilisateurDto):
    Observable<ChangerMotDePasseUtilisateurDto> {
    return this.utilisateursService.changerMotDePasse(changerMotDePasseDto);
  }

  /** CHECK LOGIN */
  isUserLoggedAndAccessTokenValid(): boolean {
    const token = this.getAccessToken();
    if (!token) {
      this.router.navigate(['login']);
      return false;
    }
    return true;
  }
}









// import { Injectable } from '@angular/core';
// import {Observable, of, throwError} from 'rxjs';
// import {
//   AuthenticationsService,
//   ChangerMotDePasseUtilisateurDto,
//   UtilisateurDto,
//   UtilisateursService
// } from '../../../gs-api/src';
// import {AuthenticationRequest} from '../../../gs-api/src/model/authenticationRequest';
// import {AuthenticationResponse} from '../../../gs-api/src/model/authenticationResponse';
// import {Router} from "@angular/router";
//
// @Injectable({
//   providedIn: 'root'
// })
// export class UserService {
//
//   constructor(
//     private authenticationsService: AuthenticationsService,
//     private utilisateursService: UtilisateursService,
//     private router: Router
//   ) {}
//
//   /**
//    * Authentifie l'utilisateur avec les identifiants fournis.
//    * En cas de succès : stocke la réponse dans le localStorage.
//    * En cas d'erreur : redirige vers la page d'inscription.
//    */
//
//   login(authenticationRequest: AuthenticationRequest): Observable<AuthenticationResponse> {
//     return this.authenticationsService.authenticate(authenticationRequest)
//   }
//
//   getUserByEmail(email?: string): Observable<UtilisateurDto>{
//     if (email !== undefined){
//       return this.utilisateursService.findByEmail(email);
//     }
//     return of();
//   }
//
//   setAccessToken(token: string): void {
//     localStorage.setItem('accessToken', token);
//   }
//
//   getAccessToken(): string | null {
//     return localStorage.getItem('accessToken');
//   }
//
//   // setAccessToken(authenticationResponse: AuthenticationResponse): void{
//   //   localStorage.setItem('accessToken', JSON.stringify(authenticationResponse));
//   // }
//
//   setConnectedUser(utilisateur: UtilisateurDto): void {
//     localStorage.setItem('connectedUser', JSON.stringify(utilisateur));
//   }
//
//   getConnectedUser(): UtilisateurDto | null{
//     if (localStorage.getItem('connectedUser')){
//       return JSON.parse(localStorage.getItem('connectedUser') as string);
//     }
//     return {};
//   }
//
//   changerMotDePasse(changerMotDePasseDto: ChangerMotDePasseUtilisateurDto):
//     Observable<ChangerMotDePasseUtilisateurDto>{
//     return this.utilisateursService.changerMotDePasse(changerMotDePasseDto);
//   }
//
//
//   // TODO
//   isUserLoggedAndAccessTokenValid(): boolean{
//     if (localStorage.getItem('accessToken')){
//       // TODO ilfaut verifier si le access token est valid
//       return true;
//     }
//     this.router.navigate(['login']);
//     return true;
//   }
// }





// import { Injectable } from '@angular/core';
// import {
//   AuthenticationsService,
//   UtilisateursService,
//   ChangerMotDePasseUtilisateurDto,
//   UtilisateurDto
// } from '../../../gs-api/src';
// import { AuthenticationRequest } from '../../../gs-api/src/model/authenticationRequest';
// import { AuthenticationResponse } from '../../../gs-api/src/model/authenticationResponse';
// import { Observable, of } from 'rxjs';
//
// @Injectable({ providedIn: 'root' })
// export class UserService {
//
//   constructor(
//     private authService: AuthenticationsService,
//     private usersService: UtilisateursService
//   ) {}
//
//   login(payload: AuthenticationRequest): Observable<AuthenticationResponse> {
//     return this.authService.authenticate(payload);
//   }
//
//   getUserByEmail(email?: string): Observable<UtilisateurDto> {
//     return email ? this.usersService.findByEmail(email) : of();
//   }
//
//   setAccessToken(token: AuthenticationResponse): void {
//     localStorage.setItem('accessToken', JSON.stringify(token));
//   }
//
//   setConnectedUser(u: UtilisateurDto): void {
//     localStorage.setItem('connectedUser', JSON.stringify(u));
//   }
//
//   getConnectedUser(): UtilisateurDto | null {
//     const data = localStorage.getItem('connectedUser');
//     return data ? JSON.parse(data) : null;
//   }
//
//   changerMotDePasse(dto: ChangerMotDePasseUtilisateurDto) {
//     return this.usersService.changerMotDePasse(dto);
//   }
//
//   isLogged(): boolean {
//     return !!localStorage.getItem('accessToken');
//   }
// }














