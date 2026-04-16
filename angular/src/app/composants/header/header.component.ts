// Import du décorateur Component et de l’interface OnInit
// Component → permet de déclarer un composant Angular
// OnInit → interface pour exécuter du code au chargement du composant
import { Component, OnInit } from '@angular/core';

// Service personnalisé qui gère les informations utilisateur
import { UserService } from "../../services/user/user.service";

// DTO (Data Transfer Object) représentant la structure d’un utilisateur
// Ce modèle provient de ton API générée (OpenAPI / Swagger)
import {PhotoService, UtilisateurDto} from "../../../gs-api/src";
import {Router} from "@angular/router";
/**
 * Décorateur @Component
 * - selector → nom utilisé dans le HTML pour appeler ce composant
 * - templateUrl → fichier HTML associé
 * - styleUrls → fichier(s) SCSS associé(s)
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  /**
   * connectedUser
   * Contient les informations de l’utilisateur connecté.
   *
   * Type : UtilisateurDto | null
   * - UtilisateurDto → si un utilisateur est connecté
   * - null → si aucun utilisateur n’est connecté
   */
  connectedUser: UtilisateurDto | null = null;
  imgUrl: string | ArrayBuffer = 'assets/profil.png';

  /**
   * Constructeur
   * Injection du UserService pour accéder aux données
   * de l’utilisateur connecté
   */
  constructor(
    private router: Router,
    private userService: UserService,
    private photoService: PhotoService,
  ) { }

  /**
   * ngOnInit()
   * Méthode appelée automatiquement par Angular
   * lors de l’initialisation du composant.
   *
   * Ici, on récupère l’utilisateur actuellement connecté
   * depuis le UserService.
   */
  ngOnInit(): void {

    // Récupération de l’utilisateur connecté
    // Cette méthode retourne généralement :
    // - un objet UtilisateurDto si l’utilisateur est connecté
    // - null si aucun utilisateur n’est authentifié
    // this.connectedUser = this.userService.getConnectedUser();

    // On s'abonne au flux de l'utilisateur
    this.userService.currentUser$.subscribe(user => {
      this.connectedUser = user;
    });
  }

  /**
   * Procède à la déconnexion de l'utilisateur
   */
  onLogout(): void {
    // 1. Suppression des données d'authentification
    localStorage.clear(); // Ou localStorage.removeItem('token');
    sessionStorage.clear();

    // 2. Redirection vers la page de login
    // On utilise then() pour s'assurer que la navigation est terminée
    this.router.navigate(['/login']).then(() => {
      console.log('Utilisateur déconnecté et redirigé.');
    });
  }
}







// ngOnInit(): void {
//   this.userService.connectedUser$.subscribe(user => {
//     this.connectedUser = user;
//     console.log("👤 ConnectedUser chargé (via observable) :", this.connectedUser);
//   });
// }


