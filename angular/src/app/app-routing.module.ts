import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {PageLoginComponent} from "./pages/page-login/page-login.component";
import {PageInscriptionComponent} from "./pages/page-inscription/page-inscription.component";
import {PageDashboardComponent} from "./pages/page-dashboard/page-dashboard.component";
import {PageStatistiquesComponent} from "./pages/page-statistiques/page-statistiques.component";
import {PageArticleComponent} from "./pages/articles/page-article/page-article.component";
import {NouvelArticleComponent} from "./pages/articles/nouvel-article/nouvel-article.component";
import {PageMvtstockComponent} from "./pages/mvtstock/page-mvtstock/page-mvtstock.component";
import {PageClientComponent} from "./pages/client/page-client/page-client.component";
import {PageFournisseurComponent} from "./pages/fournisseur/page-fournisseur/page-fournisseur.component";
import {NouveauClientFournisseurComponent} from "./composants/nouveau-client-fournisseur/nouveau-client-fournisseur.component";
import {PageCommandeClientFournisseurComponent} from "./pages/page-commande-client-fournisseur/page-commande-client-fournisseur.component";
import {NouvelleCommandeClientFournisseurComponent} from "./composants/nouvelle-commande-client-fournisseur/nouvelle-commande-client-fournisseur.component";
import {PageCategoriesComponent} from "./pages/categories/page-categories/page-categories.component";
import {NouvelleCategoryComponent} from "./pages/categories/nouvelle-category/nouvelle-category.component";
import {PageUtilisateurComponent} from "./pages/utilisateur/page-utilisateur/page-utilisateur.component";
import {NouvelUtilisateurComponent} from "./pages/utilisateur/nouvel-utilisateur/nouvel-utilisateur.component";
import {PageProfilComponent} from "./pages/profil/page-profil/page-profil.component";
import {ChangerMotDePasseComponent} from "./pages/profil/changer-mot-de-passe/changer-mot-de-passe.component";
import {ApplicationGuardService} from "./services/guard/application-guard.service";

const routes: Routes = [
  {
    path: 'login',
    component: PageLoginComponent
  },

  {
    path: 'inscrire',
    component: PageInscriptionComponent
  },

  {
    path: '',
    component: PageDashboardComponent,
    canActivate: [ApplicationGuardService],
    children: [
      {
        path: 'statistiques',
        component: PageStatistiquesComponent,
        canActivate: [ApplicationGuardService]
      },

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

      {
        path: 'nouvelarticle/:idArticle',
        component: NouvelArticleComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'mvtstock',
        component: PageMvtstockComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'clients',
        component: PageClientComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'nouveauclient',
        component: NouveauClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },

      {
        path: 'nouveauclient/:id',
        component: NouveauClientFournisseurComponent,
        canActivate: [ApplicationGuardService],
        data: {
          origin: 'client'
        }
      },

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
        path: 'profil',
        component: PageProfilComponent,
        canActivate: [ApplicationGuardService]
      },

      {
        path: 'changermotdepasse',
        component: ChangerMotDePasseComponent,
        canActivate: [ApplicationGuardService]
      },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
