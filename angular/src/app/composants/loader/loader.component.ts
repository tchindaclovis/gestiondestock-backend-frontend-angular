import { Component, OnInit } from '@angular/core';
import {LoaderService} from "./service/loader.service";
import {Subscription} from "rxjs";
import {LoaderState} from "./loader.model";

@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss']
})
export class LoaderComponent implements OnInit {

  show = false;
  subscription: Subscription | undefined; //créer un objet

  constructor(
    private loaderService: LoaderService  //j'injecte mon objet loaderService
  ) { }

  ngOnInit(): void {
    this.subscription = this.loaderService.loaderState
      .subscribe((state: LoaderState) => {  //écouter le changement
        this.show = state.show;
      });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

}
