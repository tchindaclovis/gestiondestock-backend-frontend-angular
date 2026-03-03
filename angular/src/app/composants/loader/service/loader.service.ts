import { Injectable } from '@angular/core';
import {Subject} from "rxjs";
import {LoaderState} from "../loader.model";

@Injectable({
  providedIn: 'root'
})
export class LoaderService {

  private loaderSubject = new Subject<LoaderState>();

  loaderState = this.loaderSubject.asObservable();

  constructor() { }

  show(): void {
    this.loaderSubject.next( {show: true})
  }

  hide(): void {
    this.loaderSubject.next( {show: false})
  }
}





// import {Injectable} from "@angular/core";
// import {BehaviorSubject} from "rxjs";
// import {LoaderState} from "../loader.model";
//
// @Injectable({ providedIn: 'root' })
// export class LoaderService {
//   private count = 0;
//   private loaderSubject = new BehaviorSubject<LoaderState>({ show: false });
//   loaderState$ = this.loaderSubject.asObservable();
//
//   show() {
//     this.count++;
//     this.loaderSubject.next({ show: true });
//   }
//
//   hide() {
//     this.count--;
//     if (this.count <= 0) {
//       this.count = 0;
//       this.loaderSubject.next({ show: false });
//     }
//   }
// }

