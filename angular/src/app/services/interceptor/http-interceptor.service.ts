import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {LoaderService} from "../../composants/loader/service/loader.service";

@Injectable({
  providedIn: 'root'
})
export class HttpInterceptorService implements HttpInterceptor {

    constructor(
      private loaderService: LoaderService
    ) { }


  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      this.loaderService.show(); //au demarrage de l'intercepteur je dois afficher mon loader

    const raw = localStorage.getItem('accessToken');
    if (!raw) return this.handleRequest(req, next);

    let token = null;

    try {
      const parsed = JSON.parse(raw);
      token = parsed.accessToken;
    } catch {
      token = raw;
    }

    if (!token)
      return this.handleRequest(req, next);

    const authReq = req.clone({
      setHeaders: {
        Authorization: 'Bearer ' + token
      }
    });

    return this.handleRequest(authReq, next);
  }


  handleRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>{
      return next.handle(req)
        .pipe(tap((event: HttpEvent<any>) => { //un pipe pour vérifier c'est quoi l'évènement
          if (event instanceof HttpResponse) { //si c'est une httpResponse
            this.loaderService.hide(); //je cache mon loaderService
          }
        }, (err: any) =>{
          this.loaderService.hide();
        }));
  }
}



//   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//     this.loaderService.show; //au demarrage de l'intercepteur je dois afficher mon loader
//
//   // On récupère le token tel qu'il a été stocké : une chaîne simple
//   const token = localStorage.getItem('accessToken');
//
//   if (token) {
//     // On ajoute l'en-tête Authorization
//     const authReq = req.clone({
//       setHeaders: {
//         Authorization: 'Bearer ' + token
//       }
//     });
//
//     return this.handleRequest(authReq, next);
//   }
//
//   // Aucun token -> requête originale
//     return this.handleRequest(req, next);
// }




// import { Observable } from "rxjs";
// import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
// import {Injectable} from "@angular/core";
//
// @Injectable({
//   providedIn: 'root'
// })
// export class HttpInterceptorService implements HttpInterceptor {
//
//   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//
//     // On récupère le token tel qu'il a été stocké : une chaîne simple
//     const token = localStorage.getItem('accessToken');
//
//     if (token) {
//       // On ajoute l'en-tête Authorization
//       const authReq = req.clone({
//         setHeaders: {
//           Authorization: 'Bearer ' + token
//         }
//       });
//
//       return next.handle(authReq);
//     }
//
//     // Aucun token -> requête originale
//     return next.handle(req);
//   }
// }





// import { Injectable } from '@angular/core';
// import {HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest} from "@angular/common/http";
// import {Observable} from "rxjs";
// import {AuthenticationResponse} from "../../../gs-api/src";
//
// @Injectable({
//   providedIn: 'root'
// })
// export class HttpInterceptorService implements HttpInterceptor{
//
//   constructor() { }
//
//   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//     let authenticationResponse: AuthenticationResponse = {};
//
//     if (localStorage.getItem('accessToken')) {
//       authenticationResponse = JSON.parse(
//         localStorage.getItem('accessToken') as string
//       );
//       const authReq = req.clone({
//         headers: new HttpHeaders({
//           Authorization: 'Bearer ' + authenticationResponse.accessToken
//         })
//       });
//       return next.handle(authReq);
//     }
//     return next.handle(req);
//   }
// }



// import { Injectable } from '@angular/core';
// import {
//   HttpEvent,
//   HttpHandler,
//   HttpInterceptor,
//   HttpRequest
// } from '@angular/common/http';
// import { Observable } from 'rxjs';
//
// @Injectable()
// export class HttpInterceptorService implements HttpInterceptor {
//
//   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//
//     const rawToken = localStorage.getItem('accessToken');
//
//     // Aucun token → continuer la requête telle quelle
//     if (!rawToken) {
//       return next.handle(req);
//     }
//
//     let parsed: any;
//     try {
//       parsed = JSON.parse(rawToken);
//     } catch (e) {
//       // Token mal formé → ne pas crasher l'application
//       return next.handle(req);
//     }
//
//     // Vérifie si parsed.accessToken existe (par sécurité)
//     if (!parsed?.accessToken) {
//       return next.handle(req);
//     }
//
//     // Ne pas modifier les headers existants — utiliser setHeaders proprement
//     const authReq = req.clone({
//       setHeaders: {
//         Authorization: `Bearer ${parsed.accessToken}`
//       }
//     });
//
//     return next.handle(authReq);
//   }
// }












