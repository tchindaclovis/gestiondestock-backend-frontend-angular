# @

Cette API permet de gérer les utilisateurs d\'un système de gestion. Elle offre diverses fonctionnalités pour administrer les utilisateurs, leur rôle et leur sécurité.  ### Fonctionnalités : - Créer des profils pour chaque entreprise. - Une entreprise a un ou plusieurs utilisateurs - Paramétrer les catégories d’articles (produits)  - Une entreprise a un ou plusieurs articles (produits) - Une entreprise a un ou plusieurs clients ou utilisateur  - Passer des commande clients     - Une commande client a un seul client     - Une commande client a un ou plusieurs articles (produits)     - Une commande client effectue une sortie de stock pour les articles en question  - Passer des commandes fournisseurs     - Une commande fournisseur a un seul fournisseur     - Une commande fournisseur a un ou plusieurs articles (produits)     - Une commande fournisseur effectue une entrée de stock pour les articles en question  - Effectuer les ventes au magasin     - Une vente a un ou plusieurs articles (produits)     - Une vente effectue une sortie de stock pour les articles en question  - Consulter l’état de stock de chaque entreprise     - Voir la quantité de stock de l’article en temps réel      - Effectuer les corrections de stock (mettre à jour le stock)   ### Utilisateurs par défaut : Une fois l\'application lancée, une entreprise peut se faire enregistrer   et cela va créer automatiquement un utilisateur avec le role ADMIN qui    pourra se faire authentifier pour avoir accès aus autres fonctionnalités    de l\'application.   ### Technologies utilisées : - **JAVA17** pour le langage de développement du backend - **Spring Boot** pour le développement du backend - **JPA & Hibernate** pour l\'interaction avec la base de données - **Swagger/OpenAPI** pour la documentation de l\'API - **Spring Security** pour la gestion des rôles et de la sécurité - **Lombok** pour réduire le code boilerplate  Cette API est conçue pour être utilisée par des développeurs souhaitant gérer les utilisateurs dans leurs applications, tout en garantissant une intégration facile grâce à des mécanismes de sécurité avancés. 

The version of the OpenAPI document: v1

## Building

To install the required dependencies and to build the typescript sources run:

```console
npm install
npm run build
```

## Publishing

First build the package then run `npm publish dist` (don't forget to specify the `dist` folder!)

## Consuming

Navigate to the folder of your consuming project and run one of next commands.

_published:_

```console
npm install @ --save
```

_without publishing (not recommended):_

```console
npm install PATH_TO_GENERATED_PACKAGE/dist.tgz --save
```

_It's important to take the tgz file, otherwise you'll get trouble with links on windows_

_using `npm link`:_

In PATH_TO_GENERATED_PACKAGE/dist:

```console
npm link
```

In your project:

```console
npm link 
```

__Note for Windows users:__ The Angular CLI has troubles to use linked npm packages.
Please refer to this issue <https://github.com/angular/angular-cli/issues/8284> for a solution / workaround.
Published packages are not effected by this issue.

### General usage

In your Angular project:

```typescript
// without configuring providers
import { ApiModule } from '';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
    imports: [
        ApiModule,
        // make sure to import the HttpClientModule in the AppModule only,
        // see https://github.com/angular/angular/issues/20575
        HttpClientModule
    ],
    declarations: [ AppComponent ],
    providers: [],
    bootstrap: [ AppComponent ]
})
export class AppModule {}
```

```typescript
// configuring providers
import { ApiModule, Configuration, ConfigurationParameters } from '';

export function apiConfigFactory (): Configuration {
  const params: ConfigurationParameters = {
    // set configuration parameters here.
  }
  return new Configuration(params);
}

@NgModule({
    imports: [ ApiModule.forRoot(apiConfigFactory) ],
    declarations: [ AppComponent ],
    providers: [],
    bootstrap: [ AppComponent ]
})
export class AppModule {}
```

```typescript
// configuring providers with an authentication service that manages your access tokens
import { ApiModule, Configuration } from '';

@NgModule({
    imports: [ ApiModule ],
    declarations: [ AppComponent ],
    providers: [
      {
        provide: Configuration,
        useFactory: (authService: AuthService) => new Configuration(
          {
            basePath: environment.apiUrl,
            accessToken: authService.getAccessToken.bind(authService)
          }
        ),
        deps: [AuthService],
        multi: false
      }
    ],
    bootstrap: [ AppComponent ]
})
export class AppModule {}
```

```typescript
import { DefaultApi } from '';

export class AppComponent {
    constructor(private apiGateway: DefaultApi) { }
}
```

Note: The ApiModule is restricted to being instantiated once app wide.
This is to ensure that all services are treated as singletons.

### Using multiple OpenAPI files / APIs / ApiModules

In order to use multiple `ApiModules` generated from different OpenAPI files,
you can create an alias name when importing the modules
in order to avoid naming conflicts:

```typescript
import { ApiModule } from 'my-api-path';
import { ApiModule as OtherApiModule } from 'my-other-api-path';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [
    ApiModule,
    OtherApiModule,
    // make sure to import the HttpClientModule in the AppModule only,
    // see https://github.com/angular/angular/issues/20575
    HttpClientModule
  ]
})
export class AppModule {

}
```

### Set service base path

If different than the generated base path, during app bootstrap, you can provide the base path to your service.

```typescript
import { BASE_PATH } from '';

bootstrap(AppComponent, [
    { provide: BASE_PATH, useValue: 'https://your-web-service.com' },
]);
```

or

```typescript
import { BASE_PATH } from '';

@NgModule({
    imports: [],
    declarations: [ AppComponent ],
    providers: [ provide: BASE_PATH, useValue: 'https://your-web-service.com' ],
    bootstrap: [ AppComponent ]
})
export class AppModule {}
```

### Using @angular/cli

First extend your `src/environments/*.ts` files by adding the corresponding base path:

```typescript
export const environment = {
  production: false,
  API_BASE_PATH: 'http://127.0.0.1:8080'
};
```

In the src/app/app.module.ts:

```typescript
import { BASE_PATH } from '';
import { environment } from '../environments/environment';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [ ],
  providers: [{ provide: BASE_PATH, useValue: environment.API_BASE_PATH }],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
```

### Customizing path parameter encoding

Without further customization, only [path-parameters][parameter-locations-url] of [style][style-values-url] 'simple'
and Dates for format 'date-time' are encoded correctly.

Other styles (e.g. "matrix") are not that easy to encode
and thus are best delegated to other libraries (e.g.: [@honoluluhenk/http-param-expander]).

To implement your own parameter encoding (or call another library),
pass an arrow-function or method-reference to the `encodeParam` property of the Configuration-object
(see [General Usage](#general-usage) above).

Example value for use in your Configuration-Provider:

```typescript
new Configuration({
    encodeParam: (param: Param) => myFancyParamEncoder(param),
})
```

[parameter-locations-url]: https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#parameter-locations
[style-values-url]: https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#style-values
[@honoluluhenk/http-param-expander]: https://www.npmjs.com/package/@honoluluhenk/http-param-expander
