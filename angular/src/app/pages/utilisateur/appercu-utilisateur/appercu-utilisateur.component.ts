import { Component, OnInit } from '@angular/core';
import {PhotoService, UtilisateurDto} from "../../../../gs-api/src";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";

@Component({
  selector: 'app-appercu-utilisateur',
  templateUrl: './appercu-utilisateur.component.html',
  styleUrls: ['./appercu-utilisateur.component.scss']
})
export class AppercuUtilisateurComponent implements OnInit {

  utilisateurDto: UtilisateurDto = {
    adresse: {}
  }; //objet ou variable initialisé à vide
  errorMsg: Array<string> = [];   //ou alors errorMsg: string[] = [];
  file: File| null = null;  // objet file qui peut être null et qui va être initialisé à null
  imgUrl: string | ArrayBuffer = 'assets/product.png';

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute, //va permettre de récupérer l'id de l'utilisateur qu'on va passer en paramètre
    private utilisateurService: UtilisateurService,   //injection du nouveau service article créé dans Angular
    private photoService: PhotoService,
  ) { }

  ngOnInit(): void {
    const idUtilisateur = this.activatedRoute.snapshot.params['idUtilisateur'];
    if(idUtilisateur){
      this.utilisateurService.findUtilisateurById(idUtilisateur)
        .subscribe(utilisateur =>{
          this.utilisateurDto = utilisateur;
        });
    }
  }

  cancelClick(): void{
    this.router.navigate(['utilisateurs']);
  }

  enregistrerUtilisateur(): void {
    this.utilisateurService.enregistrerUtilisateur(this.utilisateurDto)
      .subscribe(uti =>{
        this.savePhoto(uti.id)
      }, error =>{
        this.errorMsg = error.error.errors;
      });
  }

  onFileInput(files: FileList | null): void {
    if(files) {
      this.file = files.item(0);  //pour récupérer le premier fichier à l'index 0
      if (this.file){
        const fileReader = new FileReader();
        fileReader.readAsDataURL(this.file)  //pour afficher le fichier avant de l'enregistrer
        fileReader.onload = (event) => {
          if(fileReader.result){
            this.imgUrl = fileReader.result; //je peux changer ou mettre à jour le fichier
          }
        };
      }
    }
  }


  savePhoto(idUtilisateur?: number, titre?: string): void {
    if (idUtilisateur && titre && this.file) {  //si j'ai mon idArticle et un fichier sélectionné

      this.photoService.savePhoto(
        'utilisateur',        // context
        idUtilisateur,        // id
        titre,            // title
        this.file         // file (Blob)
      ).subscribe({
        next: () => {
          this.router.navigate(['utilisateurs']);
        },
        error: (err) => {
          console.error('Erreur upload photo', err);
        }
      });
    } else {
      this.router.navigate(['utilisateurs']);
    }
  }

  autoResize(event: any) {
    event.target.style.height = 'auto';
    event.target.style.height = event.target.scrollHeight + 'px';
  }

}
