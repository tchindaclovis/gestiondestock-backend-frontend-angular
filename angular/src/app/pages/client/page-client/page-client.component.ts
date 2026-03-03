import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {ClientfournisseurService} from "../../../services/clientfournisseurs/clientfournisseur.service";
import {ClientDto} from "../../../../gs-api/src";

@Component({
  selector: 'app-page-client',
  templateUrl: './page-client.component.html',
  styleUrls: ['./page-client.component.scss']
})
export class PageClientComponent implements OnInit {
  listClient: ClientDto[] = [];
  // ou encore: listClient: Array<ClientDto>= [];
  errorMsg = '';

  constructor(
    private router: Router,
    private clientFournisseurService: ClientfournisseurService
  ) { }

  ngOnInit(): void {
    this.findAllClients()
  }

  findAllClients(): void {
    this.clientFournisseurService.findAllClients()
      .subscribe(clients =>{
        this.listClient = clients;
      });
  }

  nouveauClient(): void{
    this.router.navigate(['nouveauclient'])
  }

  handleSuppression(event: any): void {
    if(event === 'success'){
      this.findAllClients();
    } else {
      this.errorMsg = event;
    }
  }
}
