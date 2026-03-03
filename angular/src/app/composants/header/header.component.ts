import { Component, OnInit } from '@angular/core';
import {UserService} from "../../services/user/user.service";
import {UtilisateurDto} from "../../../gs-api/src";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;

  constructor(
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.connectedUser = this.userService.getConnectedUser();
  }

  // ngOnInit(): void {
  //   this.userService.connectedUser$.subscribe(user => {
  //     this.connectedUser = user;
  //     console.log("👤 ConnectedUser chargé (via observable) :", this.connectedUser);
  //   });
  // }

}




// import { Component, OnInit } from '@angular/core';
// import {UserService} from "../../services/user/user.service";
// import {UtilisateurDto} from "../../../gs-api/src";
//
// @Component({
//   selector: 'app-header',
//   templateUrl: './header.component.html',
//   styleUrls: ['./header.component.scss']
// })
// export class HeaderComponent implements OnInit {
//
//   connectedUser: UtilisateurDto | null = {};
//
//   constructor(
//     private userService: UserService
//   ) { }
//
//   ngOnInit(): void {
//     this.connectedUser = this.userService.getConnectedUser();
//   }
//
// }





// import { Component, OnInit } from '@angular/core';
// import {UserService} from "../../services/user/user.service";
// import {UtilisateurDto} from "../../../gs-api/src";
//
// @Component({
//   selector: 'app-header',
//   templateUrl: './header.component.html',
//   styleUrls: ['./header.component.scss']
// })
// export class HeaderComponent implements OnInit {
//
//   connectedUser: UtilisateurDto = {};
//
//   constructor(private userService: UserService) {}
//
//   ngOnInit(): void {
//     this.connectedUser = this.userService.getConnectedUser();
//   }
// }
