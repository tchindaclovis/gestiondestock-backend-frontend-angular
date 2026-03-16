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













