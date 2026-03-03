// import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse} from '@angular/common/http';
// import { Injectable } from '@angular/core';
// import { Observable, throwError } from 'rxjs';
// import { catchError } from 'rxjs/operators';
//
// @Injectable()
// export class HttpErrorInterceptorService implements HttpInterceptor {
//
//   intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//     return next.handle(req).pipe(
//       catchError((error: HttpErrorResponse) => {
//
//         // Cas où l’erreur backend arrive en Blob (OpenAPI + SpringDoc)
//         if (error.error instanceof Blob && error.error.type === 'application/json') {
//           return new Promise((resolve, reject) => {
//             const reader = new FileReader();
//             reader.onload = () => {
//               const json = JSON.parse(reader.result as string);
//               reject(new HttpErrorResponse({
//                 error: json,
//                 status: error.status,
//                 statusText: error.statusText
//               }));
//             };
//             reader.onerror = () => {
//               reject(error);
//             };
//             reader.readAsText(error.error);
//           });
//         }
//
//         return throwError(() => error);
//       })
//     );
//   }
// }

