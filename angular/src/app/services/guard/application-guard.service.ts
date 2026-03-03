import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import { UserService } from '../user/user.service';
import {Observable} from "rxjs";

@Injectable({ providedIn: 'root' })
export class ApplicationGuardService implements CanActivate {

  constructor(
    private userService: UserService,
    // private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    return this.userService.isUserLoggedAndAccessTokenValid();
  }
}






// import { Injectable } from '@angular/core';
// import { CanActivate, Router } from '@angular/router';
// import { UserService } from '../user/user.service';
//
// @Injectable({ providedIn: 'root' })
// export class ApplicationGuardService implements CanActivate {
//
//   constructor(
//     private userService: UserService,
//     private router: Router
//   ) {}
//
//   canActivate(): boolean {
//     if (this.userService.isLogged()) return true;
//
//     this.router.navigate(['login']);
//     return false;
//   }
// }

