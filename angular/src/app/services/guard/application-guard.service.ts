// Injectable → permet d’injecter ce service dans toute l’application
import { Injectable } from '@angular/core';

// Interfaces utilisées pour protéger les routes
// ActivatedRouteSnapshot → contient les infos de la route demandée
// RouterStateSnapshot → contient l’état complet de la navigation
// CanActivate → interface pour créer un Guard
// UrlTree → permet de rediriger vers une autre route si nécessaire
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';

// Service personnalisé qui gère l’authentification utilisateur
import { UserService } from '../user/user.service';

// Observable → utilisé si la vérification est asynchrone
import { Observable } from 'rxjs';


// Décorateur Angular indiquant que ce service
// est disponible dans toute l’application (injection globale)
@Injectable({
  providedIn: 'root'
})
export class ApplicationGuardService implements CanActivate {

  /**
   * Constructeur du Guard
   * On injecte ici le UserService qui contient la logique
   * de vérification d’authentification
   */
  constructor(
    private userService: UserService
    // private router: Router   // (optionnel) utile si on veut rediriger manuellement
  ) {}

  /**
   * Méthode obligatoire de l’interface CanActivate
   * Elle est exécutée AVANT l’activation d’une route protégée
   *
   * @param route → informations sur la route cible
   * @param state → état complet de la navigation
   *
   * @returns
   *  - true → autorise l’accès
   *  - false → bloque l’accès
   *  - UrlTree → redirige vers une autre route
   *  - Observable / Promise → si la vérification est asynchrone
   */
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    // Appel du service utilisateur pour vérifier :
    // 1 ️⃣ Si l’utilisateur est connecté
    // 2 ️⃣ Si le token JWT est toujours valide
    //
    // La méthode retourne généralement :
    // - true si tout est valide
    // - false si l’utilisateur n’est pas authentifié
    //
    // Si false est retourné, Angular bloque automatiquement l’accès à la route.
    return this.userService.isUserLoggedAndAccessTokenValid();
  }
}







// import { Injectable } from '@angular/core';
// import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
// import { UserService } from '../user/user.service';
// import {Observable} from "rxjs";
//
// @Injectable({ providedIn: 'root' })
// export class ApplicationGuardService implements CanActivate {
//
//   constructor(
//     private userService: UserService,
//     // private router: Router
//   ) {}
//
//   canActivate(
//     route: ActivatedRouteSnapshot,
//     state: RouterStateSnapshot
//   ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
//
//     return this.userService.isUserLoggedAndAccessTokenValid();
//   }
// }






