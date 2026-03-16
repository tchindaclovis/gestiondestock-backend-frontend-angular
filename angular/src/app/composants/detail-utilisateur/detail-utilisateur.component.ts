import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UtilisateurDto} from "../../../gs-api/src";
import {Router} from "@angular/router";
import {UtilisateurService} from "../../services/utilisateur/utilisateur.service";

@Component({
  selector: 'app-detail-utilisateur',
  templateUrl: './detail-utilisateur.component.html',
  styleUrls: ['./detail-utilisateur.component.scss']
})
export class DetailUtilisateurComponent implements OnInit {

  @Input()
  // utilisateurDto: UtilisateurDto = {};
  utilisateurDto: UtilisateurDto = {
    nom: '',
    prenom: '',
    email: '',
    numTel: '',
    adresse: {
      adresse1: '',
      adresse2: '',
      ville: '',
      codePostale: '',
      pays: ''
    }
  };

  @Output()
  suppressionResult = new EventEmitter();

  constructor(
    private router: Router,
    private utilisateurService: UtilisateurService
  ) { }

  ngOnInit(): void {
  }


  modifierUtilisateur(): void {
    this.router.navigate(['nouvelutilisateur', this.utilisateurDto.id]);
  }


  confirmerEtSupprimerArt(): void {
    if (this.utilisateurDto.id){
    this.utilisateurService.deleteUtilisateur(this.utilisateurDto.id)
      .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
        this.suppressionResult.emit('success');
      }, error => {
        this.suppressionResult.emit(error.error.error);
      });
    }
  }

  appercuUtilisateur(): void {
    this.router.navigate(['appercuutilisateur', this.utilisateurDto.id]);
  }
}
