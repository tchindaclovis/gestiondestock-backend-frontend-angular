// Import du module NgModule permettant de déclarer un module Angular
import { NgModule } from '@angular/core';

// Import du module de routing Angular
// RouterModule permet de configurer les routes de l'application
// Routes est une interface décrivant la structure des routes
import { RouterModule, Routes } from '@angular/router';


// ==============================
// IMPORT DES COMPOSANTS PAGES
// ==============================

// Page de connexion
import { PageLoginComponent } from "./pages/page-login/page-login.component";

// Page d'inscription utilisateur
import { PageInscriptionComponent } from "./pages/page-inscription/page-inscription.component";

// Dashboard principal de l'application
import { PageDashboardComponent } from "./pages/page-dashboard/page-dashboard.component";

// Page statistiques
import { PageStatistiquesComponent } from "./pages/page-statistiques/page-statistiques.component";

// Page liste des articles
import { PageArticleComponent } from "./pages/articles/page-article/page-article.component";

// Page création / modification article
import { NouvelArticleComponent } from "./pages/articles/nouvel-article/nouvel-article.component";

// Page mouvements de stock
import { PageMvtstockComponent } from "./pages/mvtstock/page-mvtstock/page-mvtstock.component";

// Page liste clients
import { PageClientComponent } from "./pages/client/page-client/page-client.component";

// Page liste fournisseurs
import { PageFournisseurComponent } from "./pages/fournisseur/page-fournisseur/page-fournisseur.component";

// Composant création / modification client ou fournisseur
import { NouveauClientFournisseurComponent } from "./composants/nouveau-client-fournisseur/nouveau-client-fournisseur.component";

// Page liste commandes client / fournisseur
import { PageCommandeClientFournisseurComponent } from "./pages/page-commande-client-fournisseur/page-commande-client-fournisseur.component";

// Page création commande client / fournisseur
import { NouvelleCommandeClientFournisseurComponent } from "./composants/nouvelle-commande-client-fournisseur/nouvelle-commande-client-fournisseur.component";

// Page liste catégories
import { PageCategoriesComponent } from "./pages/categories/page-categories/page-categories.component";

// Page création catégorie
import { NouvelleCategoryComponent } from "./pages/categories/nouvelle-category/nouvelle-category.component";

// Page liste utilisateurs
import { PageUtilisateurComponent } from "./pages/utilisateur/page-utilisateur/page-utilisateur.component";

// Page création utilisateur
import { NouvelUtilisateurComponent } from "./pages/utilisateur/nouvel-utilisateur/nouvel-utilisateur.component";

// Page profil utilisateur connecté
import { PageProfilComponent } from "./pages/profil/page-profil/page-profil.component";

// Page changement mot de passe
import { ChangerMotDePasseComponent } from "./pages/profil/changer-mot-de-passe/changer-mot-de-passe.component";


// ==============================
// IMPORT DU GUARD DE SECURITE
// ==============================

// Guard permettant de protéger les routes
// Vérifie si l'utilisateur est authentifié avant d'accéder aux pages
import { ApplicationGuardService } from "./services/guard/application-guard.service";
import {AppercuArticleComponent} from "./pages/articles/appercu-article/appercu-article.component";
import {AppercuUtilisateurComponent} from "./pages/utilisateur/appercu-utilisateur/appercu-utilisateur.component";
import {
  AppercuClientFournisseurComponent
} from "./composants/appercu-client-fournisseur/appercu-client-fournisseur.component";


// ==============================
// CONFIGURATION DES ROUTES
// ==============================

// Tableau contenant toutes les routes de l'application
const routes: Routes = [

  // ==============================
  // ROUTES PUBLIQUES
  // ==============================

  // Page de connexion
  {
    path: 'login',
    component: PageLoginComponent
  },

  // Page d'inscription
  {
    path: 'inscrire',
    component: PageInscriptionComponent
  },


  // ==============================
  // ROUTE PRINCIPALE (DASHBOARD)
  // ==============================

  {
    // Route racine
    path: '',

    // Composant principal contenant le layout
    component: PageDashboardComponent,

    // Guard de protection
    canActivate: [ApplicationGuardService],

    // Routes enfants affichées dans le router-outlet du dashboard
    children: [

      // ==============================
      // STATISTIQUES
      // ==============================

      {
        path: 'statistiques',
        component: PageStatistiquesComponent,
        canActivate: [ApplicationGuardService]
      },


      // ==============================
      // ARTICLES
      // ==============================

      {
        path: 'articles',
        component: PageArticleComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouvelarticle',
        component: NouvelArticleComponent,
        canActivate: [ApplicationGuardService]
      },

      // Modification d'un article (id passé dans l'URL)
      {
        path: 'nouvelarticle/:idArticle',
        component: NouvelArticleComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'appercuarticle/:idArticle',
        component: AppercuArticleComponent,
        canActivate: [ApplicationGuardService]
      },


      // ==============================
      // MOUVEMENTS DE STOCK
      // ==============================

      {
        path: 'mvtstock', // Chemin unique
        component: PageMvtstockComponent,
        canActivate: [ApplicationGuardService],
      },



      // ==============================
      // CLIENTS
      // ==============================

      {
        path: 'clients',
        component: PageClientComponent,
        canActivate: [ApplicationGuardService]
      },

      // Création client
      {
        path: 'nouveauclient',
        component: NouveauClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },

      // Modification client
      {
        path: 'nouveauclient/:id',
        component: NouveauClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },

      {
        path: 'appercuclient/:id',
        component: AppercuClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },


      // ==============================
      // COMMANDES CLIENT
      // ==============================

      {
        path: 'commandesclient',
        component: PageCommandeClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },

      {
        path: 'nouvellecommandeclient',
        component: NouvelleCommandeClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },

      {
        path: 'nouvellecommandeclient/:idCommande',
        component: NouvelleCommandeClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },


      // ==============================
      // FOURNISSEURS
      // ==============================

      {
        path: 'fournisseurs',
        component: PageFournisseurComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouveaufournisseur',
        component: NouveauClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'fournisseur'
        }
      },

      {
        path: 'nouveaufournisseur/:id',
        component: NouveauClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'fournisseur'
        }
      },

      {
        path: 'appercufournisseur/:id',
        component: AppercuClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'fournisseur'
        }
      },


      // ==============================
      // COMMANDES FOURNISSEUR
      // ==============================

      {
        path: 'commandesfournisseur',
        component: PageCommandeClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'fournisseur'
        }
      },

      {
        path: 'nouvellecommandefournisseur',
        component: NouvelleCommandeClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'fournisseur'
        }
      },

      {
        path: 'nouvellecommandefournisseur/:idCommande',
        component: NouvelleCommandeClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'fournisseur'
        }
      },


      // ==============================
      // CATEGORIES
      // ==============================

      {
        path: 'categories',
        component: PageCategoriesComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouvellecategorie',
        component: NouvelleCategoryComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouvellecategorie/:idCategory',
        component: NouvelleCategoryComponent,
        canActivate: [ApplicationGuardService]
      },


      // ==============================
      // UTILISATEURS
      // ==============================

      {
        path: 'utilisateurs',
        component: PageUtilisateurComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouvelutilisateur',
        component: NouvelUtilisateurComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouvelutilisateur/:idUtilisateur',
        component: NouvelUtilisateurComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'appercuutilisateur/:idUtilisateur',
        component: AppercuUtilisateurComponent,
        canActivate: [ApplicationGuardService]
      },


      // ==============================
      // PROFIL UTILISATEUR
      // ==============================

      {
        path: 'profil',
        component: PageProfilComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'changermotdepasse',
        component: ChangerMotDePasseComponent,
        canActivate: [ApplicationGuardService]
      }

    ]
  }
];


// ==============================
// MODULE DE ROUTING
// ==============================

@NgModule({

  // Configuration du router avec les routes définies
  imports: [RouterModule.forRoot(routes)],

  // Export pour rendre le router accessible dans toute l'application
  exports: [RouterModule]

})

// Module Angular de routing principal
export class AppRoutingModule { }










// import { NgModule } from '@angular/core';
// import { RouterModule, Routes } from '@angular/router';
// import {PageLoginComponent} from "./pages/page-login/page-login.component";
// import {PageInscriptionComponent} from "./pages/page-inscription/page-inscription.component";
// import {PageDashboardComponent} from "./pages/page-dashboard/page-dashboard.component";
// import {PageStatistiquesComponent} from "./pages/page-statistiques/page-statistiques.component";
// import {PageArticleComponent} from "./pages/articles/page-article/page-article.component";
// import {NouvelArticleComponent} from "./pages/articles/nouvel-article/nouvel-article.component";
// import {PageMvtstockComponent} from "./pages/mvtstock/page-mvtstock/page-mvtstock.component";
// import {PageClientComponent} from "./pages/client/page-client/page-client.component";
// import {PageFournisseurComponent} from "./pages/fournisseur/page-fournisseur/page-fournisseur.component";
// import {NouveauClientFournisseurComponent} from "./composants/nouveau-client-fournisseur/nouveau-client-fournisseur.component";
// import {PageCommandeClientFournisseurComponent} from "./pages/page-commande-client-fournisseur/page-commande-client-fournisseur.component";
// import {NouvelleCommandeClientFournisseurComponent} from "./composants/nouvelle-commande-client-fournisseur/nouvelle-commande-client-fournisseur.component";
// import {PageCategoriesComponent} from "./pages/categories/page-categories/page-categories.component";
// import {NouvelleCategoryComponent} from "./pages/categories/nouvelle-category/nouvelle-category.component";
// import {PageUtilisateurComponent} from "./pages/utilisateur/page-utilisateur/page-utilisateur.component";
// import {NouvelUtilisateurComponent} from "./pages/utilisateur/nouvel-utilisateur/nouvel-utilisateur.component";
// import {PageProfilComponent} from "./pages/profil/page-profil/page-profil.component";
// import {ChangerMotDePasseComponent} from "./pages/profil/changer-mot-de-passe/changer-mot-de-passe.component";
// import {ApplicationGuardService} from "./services/guard/application-guard.service";
//
// const routes: Routes = [
//   {
//     path: 'login',
//     component: PageLoginComponent
//   },
//
//   {
//     path: 'inscrire',
//     component: PageInscriptionComponent
//   },
//
//   {
//     path: '',
//     component: PageDashboardComponent,
//     canActivate: [ApplicationGuardService],
//     children: [
//       {
//         path: 'statistiques',
//         component: PageStatistiquesComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'articles',
//         component: PageArticleComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvelarticle',
//         component: NouvelArticleComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvelarticle',
//         component: NouvelArticleComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvelarticle/:idArticle',
//         component: NouvelArticleComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'mvtstock',
//         component: PageMvtstockComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'clients',
//         component: PageClientComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouveauclient',
//         component: NouveauClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'client'
//         }
//       },
//
//       {
//         path: 'nouveauclient/:id',
//         component: NouveauClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'client'
//         }
//       },
//
//       {
//         path: 'commandesclient',
//         component: PageCommandeClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'client'
//         }
//       },
//
//       {
//         path: 'nouvellecommandeclient',
//         component: NouvelleCommandeClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'client'
//         }
//       },
//
//       {
//         path: 'fournisseurs',
//         component: PageFournisseurComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouveaufournisseur',
//         component: NouveauClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'fournisseur'
//         }
//       },
//
//       {
//         path: 'nouveaufournisseur/:id',
//         component: NouveauClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'fournisseur'
//         }
//       },
//
//
//       {
//         path: 'commandesfournisseur',
//         component: PageCommandeClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'fournisseur'
//         }
//       },
//
//       {
//         path: 'nouvellecommandefournisseur',
//         component: NouvelleCommandeClientFournisseurComponent,
//         canActivate: [ApplicationGuardService],
//         data: {
//           origin: 'fournisseur'
//         }
//       },
//
//       {
//         path: 'categories',
//         component: PageCategoriesComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvellecategorie',
//         component: NouvelleCategoryComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvellecategorie/:idCategory',
//         component: NouvelleCategoryComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'utilisateurs',
//         component: PageUtilisateurComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvelutilisateur',
//         component: NouvelUtilisateurComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'nouvelutilisateur/:idUtilisateur',
//         component: NouvelUtilisateurComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'profil',
//         component: PageProfilComponent,
//         canActivate: [ApplicationGuardService]
//       },
//
//       {
//         path: 'changermotdepasse',
//         component: ChangerMotDePasseComponent,
//         canActivate: [ApplicationGuardService]
//       },
//     ]
//   }
// ];
//
// @NgModule({
//   imports: [RouterModule.forRoot(routes)],
//   exports: [RouterModule]
// })
// export class AppRoutingModule { }
