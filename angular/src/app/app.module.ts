import {LOCALE_ID, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PageLoginComponent } from './pages/page-login/page-login.component';
import { PageInscriptionComponent } from './pages/page-inscription/page-inscription.component';
import { PageDashboardComponent } from './pages/page-dashboard/page-dashboard.component';
import { PageStatistiquesComponent } from './pages/page-statistiques/page-statistiques.component';
import { MenuComponent } from './composants/menu/menu.component';
import { HeaderComponent } from './composants/header/header.component';
import { PageArticleComponent } from './pages/articles/page-article/page-article.component';
import { DetailArticleComponent } from './composants/detail-article/detail-article.component';
import { PaginationComponent } from './composants/pagination/pagination.component';
import { BouttonActionComponent } from './composants/boutton-action/boutton-action.component';
import { NouvelArticleComponent } from './pages/articles/nouvel-article/nouvel-article.component';
import { PageMvtstockComponent } from './pages/mvtstock/page-mvtstock/page-mvtstock.component';
import { DetailMvtstockArticleComponent } from './composants/detail-mvtstock-article/detail-mvtstock-article.component';
import { DetailMvtstockComponent } from './composants/detail-mvtstock/detail-mvtstock.component';
import { DetailClientFournisseurComponent } from './composants/detail-client-fournisseur/detail-client-fournisseur.component';
import { PageClientComponent } from './pages/client/page-client/page-client.component';
import { PageFournisseurComponent } from './pages/fournisseur/page-fournisseur/page-fournisseur.component';
import { NouveauClientFournisseurComponent } from './composants/nouveau-client-fournisseur/nouveau-client-fournisseur.component';
import { DetailCommandeComponent } from './composants/detail-commande/detail-commande.component';
import { DetailCommandeClientFournisseurComponent } from './composants/detail-commande-client-fournisseur/detail-commande-client-fournisseur.component';
import { PageCommandeClientFournisseurComponent } from './pages/page-commande-client-fournisseur/page-commande-client-fournisseur.component';
import { NouvelleCommandeClientFournisseurComponent } from './composants/nouvelle-commande-client-fournisseur/nouvelle-commande-client-fournisseur.component';
import { PageCategoriesComponent } from './pages/categories/page-categories/page-categories.component';
import { NouvelleCategoryComponent } from './pages/categories/nouvelle-category/nouvelle-category.component';
import { PageUtilisateurComponent } from './pages/utilisateur/page-utilisateur/page-utilisateur.component';
import { DetailUtilisateurComponent } from './composants/detail-utilisateur/detail-utilisateur.component';
import { NouvelUtilisateurComponent } from './pages/utilisateur/nouvel-utilisateur/nouvel-utilisateur.component';
import { PageProfilComponent } from './pages/profil/page-profil/page-profil.component';
import { ChangerMotDePasseComponent } from './pages/profil/changer-mot-de-passe/changer-mot-de-passe.component';
import {FormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {HttpInterceptorService} from "./services/interceptor/http-interceptor.service";
import { LoaderComponent } from './composants/loader/loader.component';
import { AppercuArticleComponent } from './pages/articles/appercu-article/appercu-article.component';
import { TextFieldModule } from '@angular/cdk/text-field'; //pour cela, Installe Angular CDK version identique à ton angular de base :
import { AppercuUtilisateurComponent } from './pages/utilisateur/appercu-utilisateur/appercu-utilisateur.component';
import { AppercuClientFournisseurComponent } from './composants/appercu-client-fournisseur/appercu-client-fournisseur.component';
import {registerLocaleData} from "@angular/common";
import localeFr from '@angular/common/locales/fr';
import { PageVenteComponent } from './pages/vente/page-vente/page-vente.component';
import { NouvelleVenteComponent } from './pages/vente/nouvelle-vente/nouvelle-vente.component';
import { AppercuVenteComponent } from './pages/vente/appercu-vente/appercu-vente.component';
import { DetailVenteComponent } from './composants/detail-vente/detail-vente.component';
import { DetailVenteClientComponent } from './composants/detail-vente-client/detail-vente-client.component';



registerLocaleData(localeFr);

@NgModule({
  declarations: [
    AppComponent,
    PageLoginComponent,
    PageInscriptionComponent,
    PageDashboardComponent,
    PageStatistiquesComponent,
    MenuComponent,
    HeaderComponent,
    PageArticleComponent,
    DetailArticleComponent,
    PaginationComponent,
    BouttonActionComponent,
    NouvelArticleComponent,
    PageMvtstockComponent,
    DetailMvtstockArticleComponent,
    DetailMvtstockComponent,
    DetailClientFournisseurComponent,
    PageClientComponent,
    PageFournisseurComponent,
    NouveauClientFournisseurComponent,
    DetailCommandeComponent,
    DetailCommandeClientFournisseurComponent,
    PageCommandeClientFournisseurComponent,
    NouvelleCommandeClientFournisseurComponent,
    PageCategoriesComponent,
    NouvelleCategoryComponent,
    PageUtilisateurComponent,
    DetailUtilisateurComponent,
    NouvelUtilisateurComponent,
    PageProfilComponent,
    ChangerMotDePasseComponent,
    LoaderComponent,
    AppercuArticleComponent,
    AppercuUtilisateurComponent,
    AppercuClientFournisseurComponent,
    PageVenteComponent,
    NouvelleVenteComponent,
    AppercuVenteComponent,
    DetailVenteComponent,
    DetailVenteClientComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        FormsModule,
        HttpClientModule,
        TextFieldModule
    ],
  providers: [
    { provide: HTTP_INTERCEPTORS,
      useClass: HttpInterceptorService,
      multi: true
    },

    { provide: LOCALE_ID, useValue: 'fr-FR' }
    // { provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptorService, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
