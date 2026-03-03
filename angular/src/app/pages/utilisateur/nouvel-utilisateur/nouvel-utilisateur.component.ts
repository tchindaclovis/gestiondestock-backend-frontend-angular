
// import { Component, OnInit } from '@angular/core';
// import {ActivatedRoute, Router} from "@angular/router";
// import {PhotoService, UtilisateurDto} from "../../../../gs-api/src";
// import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";
//
// @Component({
//   selector: 'app-nouvel-utilisateur',
//   templateUrl: './nouvel-utilisateur.component.html',
//   styleUrls: ['./nouvel-utilisateur.component.scss']
// })
// export class NouvelUtilisateurComponent implements OnInit {
//
//   utilisateurDto: UtilisateurDto = {
//     adresse: {
//       adresse1: '',
//       adresse2: '',
//       ville: '',
//       codePostale: '',
//       pays: ''
//     }
//   };
//   errorMsg: Array<string> = [];
//   file: File| null = null;
//   imgUrl: string | ArrayBuffer = 'assets/product.png';
//
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private utilisateurService: UtilisateurService,
//     private photoService: PhotoService,
//   ) { }
//
//   ngOnInit(): void {
//     const idUtilisateur = this.activatedRoute.snapshot.params['idUtilisateur'];
//     if(idUtilisateur){
//       this.utilisateurService.findUtilisateurById(idUtilisateur)
//         .subscribe(utilisateur =>{
//           this.utilisateurDto = {
//             ...utilisateur,
//             adresse: utilisateur.adresse || {
//               adresse1: '',
//               adresse2: '',
//               ville: '',
//               codePostale: '',
//               pays: ''
//             }
//           };
//         });
//     }
//   }
//
//   cancelClick(): void{
//     this.router.navigate(['utilisateurs']);
//   }
//
//   enregistrerUtilisateur(): void {
//     this.utilisateurService.enregistrerUtilisateur(this.utilisateurDto)
//       .subscribe(uti =>{
//         this.savePhoto(uti.id, `${uti.nom}_${uti.prenom}`)
//       }, error =>{
//         this.errorMsg = error.error.errors;
//       });
//   }
//
//   onFileInput(files: FileList | null): void {
//     if(files) {
//       this.file = files.item(0);
//       if (this.file){
//         const fileReader = new FileReader();
//         fileReader.readAsDataURL(this.file)
//         fileReader.onload = (event) => {
//           if(fileReader.result){
//             this.imgUrl = fileReader.result;
//           }
//         };
//       }
//     }
//   }
//
//   savePhoto(idUtilisateur?: number, titre?: string): void {
//     if (idUtilisateur && this.file) {
//       // Use a default title if none provided
//       const photoTitle = titre || 'profile';
//
//       this.photoService.savePhoto(
//         'utilisateur',
//         idUtilisateur,
//         photoTitle,
//         this.file
//       ).subscribe({
//         next: () => {
//           this.router.navigate(['utilisateurs']);
//         },
//         error: (err) => {
//           console.error('Erreur upload photo', err);
//           this.router.navigate(['utilisateurs']);
//         }
//       });
//     } else {
//       this.router.navigate(['utilisateurs']);
//     }
//   }
// }





// import { Component, OnInit } from '@angular/core';
// import {ActivatedRoute, Router} from "@angular/router";
// import {PhotoService, UtilisateurDto} from "../../../../gs-api/src";
// import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";
//
// @Component({
//   selector: 'app-nouvel-utilisateur',
//   templateUrl: './nouvel-utilisateur.component.html',
//   styleUrls: ['./nouvel-utilisateur.component.scss']
// })
// export class NouvelUtilisateurComponent implements OnInit {
//
//   utilisateurDto: UtilisateurDto = {
//     adresse: {} // Initialize adresse object
//   };
//   errorMsg: Array<string> = [];
//   file: File| null = null;
//   imgUrl: string | ArrayBuffer = 'assets/product.png';
//
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private utilisateurService: UtilisateurService,
//     private photoService: PhotoService,
//   ) { }
//
//   ngOnInit(): void {
//     const idUtilisateur = this.activatedRoute.snapshot.params['idUtilisateur'];
//     if(idUtilisateur){
//       this.utilisateurService.findUtilisateurById(idUtilisateur)
//         .subscribe(utilisateur =>{
//           // Ensure adresse object exists
//           this.utilisateurDto = {
//             ...utilisateur,
//             adresse: utilisateur.adresse || {}
//           };
//         });
//     } else {
//       // Initialize adresse for new user
//       this.utilisateurDto.adresse = {};
//     }
//   }
//
//   // ... rest of your methods remain the same
//   cancelClick(): void{
//     this.router.navigate(['utilisateurs']);
//   }
//
//   enregistrerUtilisateur(): void {
//     this.utilisateurService.enregistrerUtilisateur(this.utilisateurDto)
//       .subscribe(uti =>{
//         this.savePhoto(uti.id)
//       }, error =>{
//         this.errorMsg = error.error.errors;
//       });
//   }
//
//   onFileInput(files: FileList | null): void {
//     if(files) {
//       this.file = files.item(0);
//       if (this.file){
//         const fileReader = new FileReader();
//         fileReader.readAsDataURL(this.file)
//         fileReader.onload = (event) => {
//           if(fileReader.result){
//             this.imgUrl = fileReader.result;
//           }
//         };
//       }
//     }
//   }
//
//   savePhoto(idUtilisateur?: number, titre?: string): void {
//     if (idUtilisateur && titre && this.file) {
//       this.photoService.savePhoto(
//         'utilisateur',
//         idUtilisateur,
//         titre,
//         this.file
//       ).subscribe({
//         next: () => {
//           this.router.navigate(['utilisateurs']);
//         },
//         error: (err) => {
//           console.error('Erreur upload photo', err);
//         }
//       });
//     } else {
//       this.router.navigate(['utilisateurs']);
//     }
//   }
// }





import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {PhotoService, UtilisateurDto} from "../../../../gs-api/src";
import {UtilisateurService} from "../../../services/utilisateur/utilisateur.service";

@Component({
  selector: 'app-nouvel-utilisateur',
  templateUrl: './nouvel-utilisateur.component.html',
  styleUrls: ['./nouvel-utilisateur.component.scss']
})
export class NouvelUtilisateurComponent implements OnInit {

  utilisateurDto: UtilisateurDto = {}; //objet ou variable initialisé à vide
  errorMsg: Array<string> = [];   //ou alors errorMsg: string[] = [];
  file: File| null = null;  // objet file qui peut être null et qui va être initialisé à null
  imgUrl: string | ArrayBuffer = 'assets/product.png';

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute, //va permettre de récupérer l'id de l'article qu'on va passer en paramètre
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
}
