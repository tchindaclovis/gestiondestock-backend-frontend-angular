import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {PhotoService, UtilisateurDto} from "../../../../gs-api/src";
import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";
import {UserService} from "../../../services/user/user.service";

@Component({
  selector: 'app-nouvel-utilisateur',
  templateUrl: './nouvel-utilisateur.component.html',
  styleUrls: ['./nouvel-utilisateur.component.scss']
})
export class NouvelUtilisateurComponent implements OnInit {

  utilisateurDto: UtilisateurDto = {
    adresse: {}
  }; //objet ou variable initialisé à vide
  errorMsg: Array<string> = [];   //ou alors errorMsg: string[] = [];
  file: File| null = null;  // objet file qui peut être null et qui va être initialisé à null
  imgUrl: string | ArrayBuffer = 'assets/new_product1.png';

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute, //va permettre de récupérer l'id de l'utilisateur qu'on va passer en paramètre
    private userService: UserService,
    private utilisateurService: UtilisateurService,   //injection du nouveau service article créé dans Angular
    private photoService: PhotoService,
  ) { }

  ngOnInit(): void {
    const idUtilisateur = this.activatedRoute.snapshot.params['idUtilisateur'];
    if(idUtilisateur){
      this.utilisateurService.findUtilisateurById(idUtilisateur)
      .subscribe(utilisateur =>{
        this.utilisateurDto = utilisateur;

        // CORRECTION : Si l'utilisateur a une photo (URL MinIO), on l'assigne à imgUrl
        if (this.utilisateurDto.photo && this.utilisateurDto.photo.startsWith('http')) {
          this.imgUrl = this.utilisateurDto.photo;
        }
      });
    }
  }

  cancelClick(): void{
    this.router.navigate(['utilisateurs']);
  }

  enregistrerUtilisateur(): void {
    this.utilisateurService.enregistrerUtilisateur(this.utilisateurDto)
      .subscribe(uti =>{
        this.savePhoto(uti.id, uti.nom)
        // this.router.navigate(['utilisateurs']);
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
            // Mise à jour de la prévisualisation avec le nouveau fichier sélectionné
            this.imgUrl = fileReader.result; //je peux changer ou mettre à jour le fichier
          }
        };
      }
    }
  }

  // savePhoto(idUtilisateur?: number, titre?: string): void {
  //   if (idUtilisateur && titre && this.file) {  //si j'ai mon idUtilisateur et un fichier sélectionné
  //
  //     this.photoService.savePhoto(
  //       'utilisateur',        // context
  //       idUtilisateur,        // id
  //       titre,            // title
  //       this.file         // file (Blob)
  //     ).subscribe({
  //       next: () => {
  //         this.router.navigate(['utilisateurs']);
  //       },
  //       error: (err) => {
  //         console.error('Erreur upload photo', err);
  //       }
  //     });
  //   } else {
  //     this.router.navigate(['utilisateurs']);
  //   }
  // }

  savePhoto(idUtilisateur?: number, titre?: string): void {
    if (idUtilisateur && titre && this.file) {  //si j'ai mon idArticle et un fichier sélectionné

      this.photoService.savePhoto(
        'utilisateur',        // context
        idUtilisateur,        // id
        titre,            // title
        this.file         // file (Blob)
      ).subscribe({
        next: () => {


          // On recharge les infos de l'utilisateur pour avoir la nouvelle URL de photo
          this.utilisateurService.findUtilisateurById(idUtilisateur).subscribe(user => {
            this.userService.setConnectedUser(user); // Cela va notifier le Header !
          });

          this.router.navigate(['utilisateurs']);
        },
        error: (err) => {
          console.error('Erreur upload photo', err);
        }
      });
    } else {

      // On recharge les infos de l'utilisateur pour avoir la nouvelle URL de photo
      this.utilisateurService.findUtilisateurById(idUtilisateur).subscribe(user => {
        this.userService.setConnectedUser(user); // Cela va notifier le Header !
      });

      this.router.navigate(['utilisateurs']);
    }
  }



  // savePhoto(idUtilisateur?: number, titre?: string): void {
  //   if (idUtilisateur && titre && this.file) {
  //     this.photoService.savePhoto('utilisateur', idUtilisateur, titre, this.file)
  //       .subscribe({
  //         next: () => {
  //           // --- ACTION CRUCIALE ICI ---
  //           // On recharge les infos de l'utilisateur pour avoir la nouvelle URL de photo
  //           this.utilisateurService.findUtilisateurById(idUtilisateur).subscribe(user => {
  //             this.userService.setConnectedUser(user); // Cela va notifier le Header !
  //             this.router.navigate(['utilisateurs']);
  //           });
  //         }
  //       });
  //   }
  // }


  /**
   * Retourne l'URL de la photo.
   * Si la photo commence par 'http', on l'utilise directement.
   * Sinon, on affiche l'image par défaut.
   */
// Gardez cette méthode pour la sécurité, mais nous allons simplifier le HTML
  getPhotoUrl(): string | ArrayBuffer {
    return this.imgUrl;
  }

  autoResize(event: any) {
    event.target.style.height = 'auto';
    event.target.style.height = event.target.scrollHeight + 'px';
  }
}
