import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest,
  HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { LoaderService } from "../../composants/loader/service/loader.service";
import { Router } from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class HttpInterceptorService implements HttpInterceptor {

  constructor(
    private loaderService: LoaderService,
    private router: Router // Injection du Router pour la redirection
  ) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.loaderService.show();

    const raw = localStorage.getItem('accessToken');

    // Si pas de token, on traite la requête normalement (ex: login)
    if (!raw) return this.handleRequest(req, next);

    let token = null;
    try {
      const parsed = JSON.parse(raw);
      token = typeof parsed === 'object' ? parsed.accessToken : parsed;
    } catch {
      token = raw;
    }

    if (!token) return this.handleRequest(req, next);

    // Clonage de la requête avec le Header Authorization
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
        // Authorization: 'Bearer ' + token
      }
    });

    return this.handleRequest(authReq, next);
  }

  handleRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          this.loaderService.hide();
        }
      }),
      catchError((error: any) => {
        // 1. On cache toujours le loader en cas d'erreur
        this.loaderService.hide();

        // 2. Vérification de l'expiration de session (401 ou 403)
        if (error instanceof HttpErrorResponse) {
          if (error.status === 401 || error.status === 403) {
            this.forceLogout();
          }
        }

        // 3. On renvoie l'erreur pour qu'elle soit traitée par les composants si besoin
        return throwError(() => error);
      })
    );
  }

  /**
   * Nettoie le stockage et redirige vers le login
   */
  private forceLogout(): void {
    localStorage.clear();
    sessionStorage.clear();
    this.router.navigate(['login']);
  }
}








// import { Injectable } from '@angular/core';
// import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';
// import {Observable, tap} from 'rxjs';
// import {LoaderService} from "../../composants/loader/service/loader.service";
//
// @Injectable({
//   providedIn: 'root'
// })
// export class HttpInterceptorService implements HttpInterceptor {
//
//     constructor(
//       private loaderService: LoaderService
//     ) { }
//
//
//   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//       this.loaderService.show(); //au demarrage de l'intercepteur je dois afficher mon loader
//
//     const raw = localStorage.getItem('accessToken');
//     if (!raw) return this.handleRequest(req, next);
//
//     let token = null;
//
//     try {
//       const parsed = JSON.parse(raw);
//       token = parsed.accessToken;
//     } catch {
//       token = raw;
//     }
//
//     if (!token)
//       return this.handleRequest(req, next);
//
//     const authReq = req.clone({
//       setHeaders: {
//         Authorization: 'Bearer ' + token
//       }
//     });
//
//     return this.handleRequest(authReq, next);
//   }
//
//
//   handleRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>{
//       return next.handle(req)
//         .pipe(tap((event: HttpEvent<any>) => { //un pipe pour vérifier c'est quoi l'évènement
//           if (event instanceof HttpResponse) { //si c'est une httpResponse
//             this.loaderService.hide(); //je cache mon loaderService
//           }
//         }, (err: any) =>{
//           this.loaderService.hide();
//         }));
//   }
// }













