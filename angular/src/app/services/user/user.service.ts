import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, of, switchMap, throwError} from 'rxjs';
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

  private currentUserSubject = new BehaviorSubject<UtilisateurDto | null>(this.getConnectedUser());
  public currentUser$ = this.currentUserSubject.asObservable();

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
    this.currentUserSubject.next(utilisateur); // On diffuse la mise à jour !
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














