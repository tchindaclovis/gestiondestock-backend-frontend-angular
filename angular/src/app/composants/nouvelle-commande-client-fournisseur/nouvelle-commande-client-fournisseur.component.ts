import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {
  ArticleDto,
  CommandeClientDto,
  CommandeFournisseurDto,
  LigneCommandeClientDto
} from "../../../gs-api/src";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";
import {ArticleService} from "../../services/article/article.service";
import {
  CommandeclientfournisseurService
} from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";

@Component({
  selector: 'app-nouvelle-commande-client-fournisseur',
  templateUrl: './nouvelle-commande-client-fournisseur.component.html',
  styleUrls: ['./nouvelle-commande-client-fournisseur.component.scss']
})
export class NouvelleCommandeClientFournisseurComponent implements OnInit {

  origin = '';
  selectedClientFournisseur: any = {};
  // selectedClientFournisseur: ClientDto = {};
  listClientsFournisseurs: Array<any> = [];
  searchedArticle: ArticleDto = {};
  listArticle: Array<ArticleDto> = [];
  articleErrorMsg = '';
  codeArticle = '';
  quantite = '';
  codeCommande = '';

  lignesCommande: Array<any> = [];
  totalCommande = 0 ;
  articleNotYetSelected = false;
  errorMsg: Array<string> = [];
  user: any;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService:ClientfournisseurService,
    private articleService: ArticleService,
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }


  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data =>{
      this.origin = data['origin'];
    });
    this.findAllClientsFournisseurs();
    this.findAllArticles();
  }


  cancelClick(): void{
    if(this.origin === 'client'){
      this.router.navigate(['commandesclient']);
    } else if(this.origin === 'fournisseur'){
      this.router.navigate(['commandesfournisseur']);
    }
  }


  findAllClientsFournisseurs(): void {
    if (this.origin === 'client') {
      this.clientFournisseurService.findAllClients()
        .subscribe(clients => {
          this.listClientsFournisseurs = clients;
        });
    } else if (this.origin === 'fournisseur' ) {
      this.clientFournisseurService.findAllFournisseurs()
        .subscribe(fournisseurs => {
          this.listClientsFournisseurs = fournisseurs;
        });
    }
  }



  // findAll(): void{
  //   if(this.origin === 'client'){
  //     this.clientFournisseurService.findAllClients()
  //       .subscribe(clients => {
  //         this.listClientsFournisseurs = clients;
  //       });
  //   }else     if(this.origin === 'fournisseur'){
  //     this.clientFournisseurService.findAllFournisseurs()
  //       .subscribe(fournisseurs => {
  //         this.listClientsFournisseurs = fournisseurs;
  //       });
  //   }
  // }



  findAllArticles(): void{
    this.articleService.findAllArticle()
      .subscribe(articles =>{
        this.listArticle = articles;
      });
  }


  findArticleByCode(codeArticle: string): void{
    this.articleErrorMsg = '';
    if(codeArticle){
      this.articleService.findArticleByCode(codeArticle)
        .subscribe(article =>{
          this.searchedArticle = article;
        }, error=> {
          this.articleErrorMsg = error.error.message;
        });
    }
  }


  filtrerArticle(): void {
    if(this.codeArticle.length === 0){
      this.findAllArticles()
    }
    this.listArticle = this.listArticle
      .filter(art => art.codeArticle?.includes(this.codeArticle) || art.designation?.includes(this.codeArticle));
  }



  ajouterLigneCommande(): void {
    this.checkLigneCommande();
    this.calculerTotalCommande()

    this.searchedArticle = {};
    this.quantite = '';
    this.codeArticle = '';
    this.articleNotYetSelected = false;  //je dois pouvoir sélectionner à nouveau mon article
    this.findAllArticles();
  }



  calculerTotalCommande(): void{
    this.totalCommande = 0 ;
    this.lignesCommande.forEach(ligne => {
      if(ligne.prixUnitaire && ligne.quantite){
        this.totalCommande += +ligne.prixUnitaire * +ligne.quantite;
      }
    });
  }


  private checkLigneCommande(): void{
    const ligneCommandeAlreadyExists = this.lignesCommande
      .find(lig => lig.article?.codeArticle === this.searchedArticle.codeArticle);
    if (ligneCommandeAlreadyExists){
      this.lignesCommande.forEach(lig => {
        if(lig && lig.article?.codeArticle === this.searchedArticle.codeArticle){
          //@ts-ignore
          lig.quantite = lig.quantite + +this.quantite;
        }
      });
    } else {

      const ligneCmd: LigneCommandeClientDto = {  //je cré l'objet ligne de commande
        article: this.searchedArticle,
        prixUnitaire: this.searchedArticle.prixUnitaireTtc,
        quantite: +this.quantite
      };
      this.lignesCommande.push(ligneCmd);
    }
  }


  selectArticleClick(article: ArticleDto): void {
    this.searchedArticle = article;
    this.codeArticle = article.codeArticle ? article.codeArticle : '';
    this.articleNotYetSelected = true;
  }


  enregistrerCommande(): void {
    const commande = this.preparerCommande();
    if (this.origin === 'client') {
      this.commandeClientFournisseurService.enregistrerCommandeClient(commande as CommandeClientDto)
        .subscribe(cmd => {
          this.router.navigate(['commandesclient'])
        }, error => {
          if (error.error instanceof Blob) {
            error.error.text().then((text: string) => {
              const err = JSON.parse(text);
              this.errorMsg = err.errors?.length ? err.errors : [err.message];
            });
          } else {
            this.errorMsg = error.error?.errors?.length
              ? error.error.errors
              : [error.error?.message ?? 'Erreur inconnue'];
          }
        });
    } else if (this.origin === 'fournisseur') {
      this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande as CommandeFournisseurDto)
        .subscribe(cmd => {
          this.router.navigate(['commandesfournisseur'])
        }, error => {
          if (error.error instanceof Blob) {
            error.error.text().then((text: string) => {
              const err = JSON.parse(text);
              this.errorMsg = err.errors?.length ? err.errors : [err.message];
            });
          } else {
            this.errorMsg = error.error?.errors?.length
              ? error.error.errors
              : [error.error?.message ?? 'Erreur inconnue'];
          }
        });
    }
  }


  private preparerCommande(): any {
    if (this.origin === 'client') {
      return  {
        client: this.selectedClientFournisseur,
        code: this.codeCommande,
        dateCommande: new Date().getTime(),
        etatCommande: 'EN_PREPARATION',

        ligneCommandeClients: this.lignesCommande

      };
    } else if (this.origin === 'fournisseur') {
      return  {
        fournisseur: this.selectedClientFournisseur,
        code: this.codeCommande,
        dateCommande: new Date().getTime(),
        etatCommande: 'EN_PREPARATION',
        ligneCommandeFournisseurs: this.lignesCommande
      };
    }
  }


  // private preparerCommande(): any {
  //
  //   if (!this.selectedClientFournisseur || !this.selectedClientFournisseur.idEntreprise) {
  //     this.errorMsg = ['Entreprise non définie pour le client/fournisseur sélectionné'];
  //     return;
  //   }
  //
  //   const idEntreprise = this.selectedClientFournisseur.idEntreprise;
  //
  //   if (this.origin === 'client') {
  //     return {
  //       client: this.selectedClientFournisseur,
  //       code: this.codeCommande,
  //       dateCommande: new Date(),   // ✅ PAS de timestamp
  //       etatCommande: 'EN_PREPARATION',
  //       idEntreprise: idEntreprise,
  //       ligneCommandeClients: this.lignesCommande.map(ligne => ({
  //         article: ligne.article,
  //         prixUnitaire: ligne.prixUnitaire,
  //         quantite: ligne.quantite,
  //         idEntreprise: idEntreprise
  //       }))
  //     };
  //   }
  //
  //   if (this.origin === 'fournisseur') {
  //     return {
  //       fournisseur: this.selectedClientFournisseur,
  //       code: this.codeCommande,
  //       dateCommande: new Date(),
  //       etatCommande: 'EN_PREPARATION',
  //       idEntreprise: idEntreprise,
  //       ligneCommandeFournisseurs: this.lignesCommande.map(ligne => ({
  //         article: ligne.article,
  //         prixUnitaire: ligne.prixUnitaire,
  //         quantite: ligne.quantite,
  //         idEntreprise: idEntreprise
  //       }))
  //     };
  //   }
  // }

}


// private preparerCommande(): any {
//   const idEntreprise = this.selectedClientFournisseur?.idEntreprise;
//   if (this.origin === 'client') {
//     return  {
//       client: this.origin === 'client' ? this.selectedClientFournisseur : null,
//       code: this.codeCommande,
//       dateCommande: new Date(),
//       etatCommande: 'EN_PREPARATION',
//       idEntreprise: idEntreprise,
//       // lignesCommandeClients: this.lignesCommande
//       lignesCommandeClients: this.lignesCommande.map(l => ({
//         ...l,
//         idEntreprise: idEntreprise
//       }))
//     };
//
//   } else if (this.origin === 'fournisseur') {
//     return  {
//       fournisseur: this.origin === 'fournisseur' ? this.selectedClientFournisseur : null,
//       code: this.codeCommande,
//       dateCommande: new Date(),
//       etatCommande: 'EN_PREPARATION',
//       idEntreprise: idEntreprise,
//       // lignesCommandeFournisseurs: this.lignesCommande
//       lignesCommandeFournisseurs: this.lignesCommande.map(l => ({
//         ...l,
//         idEntreprise: idEntreprise
//       }))
//     };
//   }
// }



// findAll(): void{
//   if(this.origin === 'client'){
//   this.clientFournisseurService.findAllClients()
//     .subscribe(clients => {
//       this.listClientsFournisseurs = clients;
//     });
// }else     if(this.origin === 'fournisseur'){
//   this.clientFournisseurService.findAllFournisseurs()
//     .subscribe(fournisseurs => {
//       this.listClientsFournisseurs = fournisseurs;
//     });
// }
// }


// enregistrerCommande(): void {
//   const commandeClient: CommandeClientDto = {
//   client: this.selectedClientFournisseur,
//   code: 'code',
//   etatCommande: 'EN_PREPARATION',
//   idEntreprise: 1
// };
// this.commandeClientService.save5(commandeClient)
//   .subscribe(cmd => {
//     this.router.navigate(['commandesclient'])
//   }, error => {
//     if (error.error instanceof Blob) {
//       error.error.text().then((text: string) => {
//         const err = JSON.parse(text);
//         this.errorMsg = err.errors?.length ? err.errors : [err.message];
//       });
//     } else {
//       this.errorMsg = error.error?.errors?.length
//         ? error.error.errors
//         : [error.error?.message ?? 'Erreur inconnue'];
//     }
//   });
// }
