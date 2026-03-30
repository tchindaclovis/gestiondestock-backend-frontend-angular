import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {AdresseDto, ClientDto, FournisseurDto, PhotoService} from "../../../gs-api/src";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";

@Component({
  selector: 'app-nouveau-client-fournisseur',
  templateUrl: './nouveau-client-fournisseur.component.html',
  styleUrls: ['./nouveau-client-fournisseur.component.scss']
})
export class NouveauClientFournisseurComponent implements OnInit {

  // creation des objets
  origin = '';
  clientFournisseur: any = {};  //any parceque ça peut être un client ou un fournisseur
  adresseDto: AdresseDto = {};
  errorMsg: Array<string> = [];
  file: File| null = null;  // objet file qui peut être null et qui va être initialisé à null
  imgUrl: string | ArrayBuffer = 'assets/product.png';

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private photoService: PhotoService
  ) { }


  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['origin'];
      // console.log('ORIGIN =', this.origin);
      // this.findClientFournisseur();
    });
    this.findClientFournisseur();
  }


  findClientFournisseur(): void{   //methode permettant de trouver soit le client soit le fournisseur
    const id = this.activatedRoute.snapshot.params['id'];
    if (id) {
      if (this.origin === 'client') {
        this.clientFournisseurService.findClientById(id)
          .subscribe(client => {
            this.clientFournisseur = client;
            this.adresseDto = this.clientFournisseur.adresse;
          });
      } else if (this.origin === 'fournisseur') {
        this.clientFournisseurService.findFournisseurById(id)
          .subscribe(fournisseur => {
            this.clientFournisseur = fournisseur;
            this.adresseDto = this.clientFournisseur.adresse;
          });
      }
    }
  }


  enregistrer() {
    if(this.origin === 'client'){
      this.clientFournisseurService.enregistrerClient(this.mapToClient())
        .subscribe(client =>{
          this.savePhoto(client.id, client.nom);
        }, error =>{
          // this.errorMsg = error?.error?.errors ?? ['Une erreur est survenue'];
          this.errorMsg = error.error.errors;
        });
    } else if(this.origin === 'fournisseur'){
      this.clientFournisseurService.enregistrerFournisseur(this.mapToFournisseur())
        .subscribe(fournisseur =>{
          this.savePhoto(fournisseur.id, fournisseur.nom);
        }, error =>{
          // this.errorMsg = error?.error?.errors ?? ['Une erreur est survenue'];
          this.errorMsg = error.error.errors;
        });
    }
  }


  cancelClick(): void{
    if(this.origin === 'client'){
      this.router.navigate(['clients']);
    } else if(this.origin === 'fournisseur'){
      this.router.navigate(['fournisseurs']);
    }
  }


  mapToClient(): ClientDto{
    const clientDto: ClientDto = this.clientFournisseur;
    clientDto.adresse = this.adresseDto;
    return clientDto;
  }


  mapToFournisseur(): FournisseurDto{
    const fournisseurDto: FournisseurDto = this.clientFournisseur;
    fournisseurDto.adresse = this.adresseDto;
    return fournisseurDto;
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


  savePhoto(idObject?: number, titre?: string): void {
    if (idObject && titre && this.file) {  //si j'ai mon idArticle et un fichier sélectionné

      this.photoService.savePhoto(
        this.origin,        // context
        idObject,        // id
        titre,            // title
        this.file         // file (Blob)
      ).subscribe({
        next: () => {
          this.cancelClick();
        },
        error: (err) => {
          console.error('Erreur upload photo', err);
        }
      });

    } else {
      this.cancelClick();
    }
  }
}




// clientFournisseur: any = {   //any parceque ça peut être un client ou un fournisseur
//                              // nom: '',
//                              // prenom: '',
//                              // email: '',
//                              // numTel: ''
// };
//
// adresseDto: AdresseDto = {
//   // adresse1: '',
//   // adresse2: '',
//   // ville: '',
//   // codePostale: '',
//   // pays: ''
// };



// findClientFournisseur(): void {
// const id = this.activatedRoute.snapshot.params['id'];
// if (!id) return;
//
//   if (this.origin === 'client') {
//     this.clientFournisseurService.findClientById(id)
//       .subscribe(client => {
//         this.clientFournisseur = client ?? {};
//         this.adresseDto = client?.adresse ?? {};
//       });
//   }
//
//   if (this.origin === 'fournisseur') {
//     this.clientFournisseurService.findFournisseurById(id)
//       .subscribe(fournisseur => {
//         this.clientFournisseur = fournisseur ?? {};
//         this.adresseDto = fournisseur?.adresse ?? {};
//       });
//   }
// }



// findClientFournisseur(): void {
//   const id = this.activatedRoute.snapshot.params['id'];
//   if (!id) return;
//
//   if (this.origin === 'client') {
//     this.clientFournisseurService.findClientById(id)
//       .subscribe(client => {
//         if (!client) return;
//
//         Object.assign(this.clientFournisseur, client);
//         Object.assign(this.adresseDto, client.adresse);
//       });
//   }
//
//   if (this.origin === 'fournisseur') {
//     this.clientFournisseurService.findFournisseurById(id)
//       .subscribe(fournisseur => {
//         if (!fournisseur) return;
//
//         Object.assign(this.clientFournisseur, fournisseur);
//         Object.assign(this.adresseDto, fournisseur.adresse);
//       });
//   }
// }





// import { Component, OnInit } from '@angular/core';
// import { ActivatedRoute, Router } from "@angular/router";
// import { AdresseDto, ClientDto, FournisseurDto } from "../../../gs-api/src";
// import { ClientfournisseurService } from "../../services/clientfournisseurs/clientfournisseur.service";
//
// @Component({
//   selector: 'app-nouveau-client-fournisseur',
//   templateUrl: './nouveau-client-fournisseur.component.html',
//   styleUrls: ['./nouveau-client-fournisseur.component.scss']
// })
// export class NouveauClientFournisseurComponent implements OnInit {
//
//   origin = '';
//
//   // ✅ TOUJOURS initialisés
//   clientFournisseur: any = {};
//   adresseDto: AdresseDto = {};
//   errorMsg: string[] = [];
//
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private clientFournisseurService: ClientfournisseurService
//   ) {}
//
//   ngOnInit(): void {
//     this.activatedRoute.data.subscribe(data => {
//       this.origin = data['origin'];
//       this.findClientFournisseur();
//     });
//   }
//
//   findClientFournisseur(): void {
//     const id = this.activatedRoute.snapshot.params['id'];
//     if (!id) return;
//
//     if (this.origin === 'client') {
//       this.clientFournisseurService.findClientById(id).subscribe(client => {
//         this.clientFournisseur = client ?? {};
//         this.adresseDto = client?.adresse ?? {};
//       });
//     }
//
//     if (this.origin === 'fournisseur') {
//       this.clientFournisseurService.findFournisseurById(id).subscribe(fournisseur => {
//         this.clientFournisseur = fournisseur ?? {};
//         this.adresseDto = fournisseur?.adresse ?? {};
//       });
//     }
//   }
//
//   enregistrer(): void {
//     if (this.origin === 'client') {
//       this.clientFournisseurService.enregistrerClient(this.mapToClient())
//         .subscribe({
//           next: () => this.router.navigate(['clients']),
//           error: err => {
//             this.errorMsg = err?.error?.errors ?? ['Une erreur est survenue'];
//           }
//         });
//     }
//
//     if (this.origin === 'fournisseur') {
//       this.clientFournisseurService.enregistrerFournisseur(this.mapToFournisseur())
//         .subscribe({
//           next: () => this.router.navigate(['fournisseurs']),
//           error: err => {
//             this.errorMsg = err?.error?.errors ?? ['Une erreur est survenue'];
//           }
//         });
//     }
//   }
//
//   cancelClick(): void {
//     this.router.navigate([this.origin === 'client' ? 'clients' : 'fournisseurs']);
//   }
//
//   mapToClient(): ClientDto {
//     return {
//       ...this.clientFournisseur,
//       adresse: this.adresseDto
//     };
//   }
//
//   mapToFournisseur(): FournisseurDto {
//     return {
//       ...this.clientFournisseur,
//       adresse: this.adresseDto
//     };
//   }
// }

