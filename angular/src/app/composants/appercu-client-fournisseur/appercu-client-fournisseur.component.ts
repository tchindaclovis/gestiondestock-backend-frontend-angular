import { Component, OnInit } from '@angular/core';
import {AdresseDto, ClientDto, FournisseurDto, PhotoService} from "../../../gs-api/src";
import {ActivatedRoute, Router} from "@angular/router";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";

@Component({
  selector: 'app-appercu-client-fournisseur',
  templateUrl: './appercu-client-fournisseur.component.html',
  styleUrls: ['./appercu-client-fournisseur.component.scss']
})
export class AppercuClientFournisseurComponent implements OnInit {

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
      console.log('ORIGIN =', this.origin);
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
