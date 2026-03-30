import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Router} from "@angular/router";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";

@Component({
  selector: 'app-detail-client-fournisseur',
  templateUrl: './detail-client-fournisseur.component.html',
  styleUrls: ['./detail-client-fournisseur.component.scss']
})
export class DetailClientFournisseurComponent implements OnInit {

  @Input()
  origin = '';

  @Input()
  clientFournisseur: any = {}; //soit client soit fournisseur

  @Output()
  suppressionResult = new EventEmitter();

  constructor(
    private router: Router,
    private clientFournisseurService: ClientfournisseurService
  ) { }

  ngOnInit(): void {
  }

  modifierClientFournisseur(): void {
    if(this.origin === 'client'){
      this.router.navigate(['nouveauclient', this.clientFournisseur.id]);
    } else if(this.origin === 'fournisseur'){
      this.router.navigate(['nouveaufournisseur', this.clientFournisseur.id]);
    }
  }

  confirmerEtSupprimer(): void {
    if(this.origin === 'client'){
      this.clientFournisseurService.deleteClient(this.clientFournisseur.id)
        .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
          this.suppressionResult.emit('success');
        }, error => {
          this.suppressionResult.emit(error.error.error);
        });

    } else if(this.origin === 'fournisseur'){
      this.clientFournisseurService.deleteFournisseur(this.clientFournisseur.id)
        .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
          this.suppressionResult.emit('success');
        }, error => {
          this.suppressionResult.emit(error.error.error);
        });
    }
  }

  appercuArticle(): void {
    if(this.origin === 'client'){
      this.router.navigate(['appercuclient', this.clientFournisseur.id]);
    } else if(this.origin === 'fournisseur'){
      this.router.navigate(['appercufournisseur', this.clientFournisseur.id]);
    }
  }
}
