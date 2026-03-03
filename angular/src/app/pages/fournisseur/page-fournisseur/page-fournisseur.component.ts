import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {ClientfournisseurService} from "../../../services/clientfournisseurs/clientfournisseur.service";
import {FournisseurDto} from "../../../../gs-api/src";

@Component({
  selector: 'app-page-fournisseur',
  templateUrl: './page-fournisseur.component.html',
  styleUrls: ['./page-fournisseur.component.scss']
})
export class PageFournisseurComponent implements OnInit {

  listFournisseur: Array<FournisseurDto>= [];
  errorMsg ='';

  constructor(
    private router: Router,
    private clientFournisseurService: ClientfournisseurService
  ) { }

  ngOnInit(): void {
    this.findAllFournisseurs()
  }

  findAllFournisseurs(): void {
    this.clientFournisseurService.findAllFournisseurs()
      .subscribe(fournisseurs =>{
        this.listFournisseur = fournisseurs;
      });
  }

  nouveauFournisseur(): void{
    this.router.navigate(['nouveaufournisseur'])
  }

  handleSuppression(event: any): void {
    if(event === 'success'){
      this.findAllFournisseurs();
    } else {
      this.errorMsg = event;
    }
  }
}
