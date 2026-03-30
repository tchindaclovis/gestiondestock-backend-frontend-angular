// ==============================
// IMPORTS ANGULAR
// ==============================
import {Component, Input, OnInit} from '@angular/core';

// ActivatedRoute → récupérer les paramètres et data de la route
// Router → navigation entre les pages
import { ActivatedRoute, Router } from "@angular/router";


// ==============================
// IMPORTS MODELES (DTO)
// ==============================
import {
  ArticleDto, CategoryDto, ClientDto,
  CommandeClientDto,
  CommandeFournisseurDto, FournisseurDto,
  LigneCommandeClientDto
} from "../../../gs-api/src";


// ==============================
// IMPORTS SERVICES
// ==============================

// Service gestion client / fournisseur
import { ClientfournisseurService } from "../../services/clientfournisseurs/clientfournisseur.service";

// Service gestion articles
import { ArticleService } from "../../services/article/article.service";

// Service gestion commandes
import { CommandeclientfournisseurService } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
import {CategoryService} from "../../services/category/category.service";

// ==============================
// DECORATEUR COMPONENT
// ==============================
@Component({
  selector: 'app-nouvelle-commande-client-fournisseur',
  templateUrl: './nouvelle-commande-client-fournisseur.component.html',
  styleUrls: ['./nouvelle-commande-client-fournisseur.component.scss']
})

export class NouvelleCommandeClientFournisseurComponent implements OnInit {

  origin = '';
  clientFournisseur: any = {}; //soit client soit fournisseur

  codeArticle = '';
  quantite = '';
  codeCommande = ''; // Lié au champ "Code commande"

  selectedClientFournisseur: any = {}; // Variable utilisée dans le HTML
  listClientsFournisseurs: Array<any> = [];

  searchedArticle: ArticleDto = {};
  listArticle: Array<ArticleDto> = [];



  articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
  errorMsg : Array<string> = [];
  listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau

  lignesCommande: Array<any> = [];
  totalCommande = 0;
  articleNotYetSelected = false;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
    private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }

  ngOnInit(): void {

    // 1. Déterminer l'origine
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['origin'];

      // Charger la liste correspondante dès qu'on connaît l'origine
      this.findAllClientsFournisseurs();

      // 2. Charger les articles pour l'autocomplétion
      this.findAllArticles();

      // 3. Mode Modification / Visualisation
      // On récupère l'ID passé dans l'URL (le "103" de votre exemple)
      const id = this.activatedRoute.snapshot.params['idCommande'];
      if (id) {
        this.chargerDonneesPourModification(id);
      }
    });
  }



  chargerDonneesPourModification(id: number): void {
    console.log("ID détecté :", id, " | Origine :", this.origin);

    // On définit l'appel dynamiquement
    const serviceCall = this.origin === 'client'
      ? this.commandeClientFournisseurService.findCommandeClientById(id)
      : this.commandeClientFournisseurService.findCommandeFournisseurById(id);

    // On force le type à 'any' pour le subscribe pour éviter l'erreur TS2349
    (serviceCall as any).subscribe((res: any) => {
      this.traiterReponse(res, id);
    }, (error: any) => {
      this.handleError(error);
    });
  }

  private traiterReponse(res: any, id: number): void {
    // Cas où la réponse est un Blob (Binaire JSON)
    if (res instanceof Blob) {
      res.text().then(text => {
        const cmd = JSON.parse(text);
        this.affecterDonnees(cmd);
        this.chargerLignesCommande(id);
      });
    }
    // Cas où la réponse est déjà un objet JSON
    else {
      this.affecterDonnees(res);
      this.chargerLignesCommande(id);
    }
  }

  private affecterDonnees(cmd: any): void {
    this.codeCommande = cmd.code || '';
    // On récupère soit le client soit le fournisseur selon l'origine
    this.selectedClientFournisseur = (this.origin === 'client') ? cmd.client : cmd.fournisseur;
    console.log("Données affectées au formulaire :", this.codeCommande);
  }

  // // Fusion des méthodes pour charger à la fois le client et les lignes
  // chargerDonneesPourModification(id: number): void {
  //   console.log("ID détecté :", id, " | Origine :", this.origin);
  //   if (this.origin === 'client') {
  //     this.commandeClientFournisseurService.findCommandeClientById(id)
  //       .subscribe(cmd => {
  //         console.log("Commande Client reçue :", cmd);
  //         this.codeCommande = cmd.code || ''; // Affectation à la variable liée au ngModel
  //         this.selectedClientFournisseur = cmd.client || {};
  //         // On charge les lignes après avoir reçu la commande
  //         this.chargerLignesCommande(id);
  //       });
  //   } else {
  //     this.commandeClientFournisseurService.findCommandeFournisseurById(id)
  //       .subscribe(cmd => {
  //         console.log("Commande Fournisseur reçue :", cmd);
  //         this.codeCommande = cmd.code || '';
  //         this.selectedClientFournisseur = cmd.fournisseur || {}; // Affectation à la variable liée au ngModel
  //         this.chargerLignesCommande(id);
  //       });
  //   }
  // }



  private chargerLignesCommande(id: number): void {
    const serviceLignes = this.origin === 'client'
      ? this.commandeClientFournisseurService.findAllLigneCommandesClient(id)
      : this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(id);

    serviceLignes.subscribe((res: any) => {
      if (res instanceof Blob) {
        res.text().then(text => {
          this.lignesCommande = JSON.parse(text);
          this.calculerTotalCommande();
        });
      } else {
        this.lignesCommande = res;
        this.calculerTotalCommande();
      }
    });
  }

  // private chargerLignesCommande(id: number): void {
  //   if (this.origin === 'client') {
  //     this.commandeClientFournisseurService.findAllLigneCommandesClient(id)
  //       .subscribe(lignes => {
  //         this.lignesCommande = lignes;
  //         this.calculerTotalCommande();
  //       });
  //   } else {
  //     this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(id)
  //       .subscribe(lignes => {
  //         this.lignesCommande = lignes;
  //         this.calculerTotalCommande();
  //       });
  //   }
  // }


  calculerTotalCommande(): void {
    this.totalCommande = 0;
    this.lignesCommande.forEach(lig => {
      this.totalCommande += (lig.prixUnitaire * lig.quantite);
    });
  }


  // ==============================
  // LOGIQUE METIER (Correction des liaisons)
  // ==============================

  compareFn(c1: any, c2: any): boolean {
    return c1 && c2 ? c1.id === c2.id : c1 === c2;
  }

  findAllClientsFournisseurs(): void {
    if (this.origin === 'client') {
      this.clientFournisseurService.findAllClients()
        .subscribe(res => this.listClientsFournisseurs = res);
    } else {
      this.clientFournisseurService.findAllFournisseurs()
        .subscribe(res => this.listClientsFournisseurs = res);
    }
  }


  findAllArticles(): void {
    this.articleService.findAllArticle().subscribe(res => this.listArticle = res);
  }

  filtrerArticle(): void {
    if (!this.codeArticle) {
      this.findAllArticles();
      return;
    }
    this.listArticle = this.listArticle.filter(art =>
      art.codeArticle?.toLowerCase().includes(this.codeArticle.toLowerCase()) ||
      art.designation?.toLowerCase().includes(this.codeArticle.toLowerCase())
    );
  }

  selectArticleClick(article: ArticleDto): void {
    this.searchedArticle = article;
    this.codeArticle = article.codeArticle || '';
    this.articleNotYetSelected = true;
  }

  ajouterLigneCommande(): void {
    this.checkLigneCommande();
    this.calculerTotalCommande();
    // Reset
    this.searchedArticle = {};
    this.quantite = '';
    this.codeArticle = '';
    this.articleNotYetSelected = false;
  }

  private checkLigneCommande(): void {
    const ligneExistante = this.lignesCommande.find(lig =>
      lig.article?.codeArticle === this.searchedArticle.codeArticle
    );

    if (ligneExistante) {
      ligneExistante.quantite += +this.quantite;
    } else {
      const ligneCmd = {
        article: this.searchedArticle,
        prixUnitaire: this.searchedArticle.prixUnitaireTtc,
        quantite: +this.quantite
      };
      this.lignesCommande.push(ligneCmd);
    }
  }

  enregistrerCommande(): void {
    const commande = this.preparerCommande();
    if (this.origin === 'client') {
      this.commandeClientFournisseurService.enregistrerCommandeClient(commande)
        .subscribe(() => this.router.navigate(['commandesclient']), e => this.handleError(e));
    } else {
      this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande)
        .subscribe(() => this.router.navigate(['commandesfournisseur']), e => this.handleError(e));
    }
  }

  private preparerCommande(): any {
    return {
      [this.origin]: this.selectedClientFournisseur, // clé dynamique 'client' ou 'fournisseur'
      code: this.codeCommande,
      dateCommande: new Date().getTime(),
      etatCommande: 'EN_PREPARATION',
      [this.origin === 'client' ? 'ligneCommandeClients' : 'ligneCommandeFournisseurs']: this.lignesCommande
    };
  }

  cancelClick(): void {
    this.router.navigate([this.origin === 'client' ? 'commandesclient' : 'commandesfournisseur']);
  }

  private handleError(error: any): void {
    this.errorMsg = error.error?.errors || [error.error?.message || 'Erreur'];
  }
}






// // ==============================
// // IMPORTS ANGULAR
// // ==============================
// import { Component, OnInit } from '@angular/core';
//
// // ActivatedRoute → récupérer les paramètres et data de la route
// // Router → navigation entre les pages
// import { ActivatedRoute, Router } from "@angular/router";
//
//
// // ==============================
// // IMPORTS MODELES (DTO)
// // ==============================
// import {
//   ArticleDto, CategoryDto,
//   CommandeClientDto,
//   CommandeFournisseurDto,
//   LigneCommandeClientDto
// } from "../../../gs-api/src";
//
//
// // ==============================
// // IMPORTS SERVICES
// // ==============================
//
// // Service gestion client / fournisseur
// import { ClientfournisseurService } from "../../services/clientfournisseurs/clientfournisseur.service";
//
// // Service gestion articles
// import { ArticleService } from "../../services/article/article.service";
//
// // Service gestion commandes
// import { CommandeclientfournisseurService } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
// import {CategoryService} from "../../services/category/category.service";
//
//
// // ==============================
// // DECORATEUR COMPONENT
// // ==============================
// @Component({
//   selector: 'app-nouvelle-commande-client-fournisseur',
//   templateUrl: './nouvelle-commande-client-fournisseur.component.html',
//   styleUrls: ['./nouvelle-commande-client-fournisseur.component.scss']
// })
// export class NouvelleCommandeClientFournisseurComponent implements OnInit {
//
//   // ==============================
//   // VARIABLES PRINCIPALES
//   // ==============================
//
//   /**
//    * origin → permet de savoir si on travaille avec :
//    * - client
//    * - fournisseur
//    * (valeur récupérée depuis la route)
//    */
//   origin = '';
//
//   /**
//    * Client ou fournisseur sélectionné
//    */
//   selectedClientFournisseur: any = {};
//
//   /**
//    * Liste des clients ou fournisseurs
//    */
//   listClientsFournisseurs: Array<any> = [];
//
//   /**
//    * Article sélectionné pour ajout dans la commande
//    */
//   searchedArticle: ArticleDto = {};
//
//   clientFournisseur: any = {};  //any parceque ça peut être un client ou un fournisseur
//   articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
//   categorieDto: CategoryDto = {};
//   listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau
//
//   /**
//    * Liste des articles disponibles
//    */
//   listArticle: Array<ArticleDto> = [];
//
//   /**
//    * Message d’erreur lors de la recherche d’article
//    */
//   articleErrorMsg = '';
//
//   /**
//    * Champs de saisie utilisateur
//    */
//   codeArticle = '';
//   quantite = '';
//   codeCommande = '';
//
//   /**
//    * Liste des lignes de commande
//    */
//   lignesCommande: Array<any> = [];
//
//   /**
//    * Total de la commande
//    */
//   totalCommande = 0;
//
//   /**
//    * Indique si un article a été sélectionné
//    */
//   articleNotYetSelected = false;
//
//   /**
//    * Liste des erreurs backend
//    */
//   errorMsg: Array<string> = [];
//
//   /**
//    * Utilisateur connecté (non utilisé ici)
//    */
//   user: any;
//
//
//   // ==============================
//   // CONSTRUCTEUR
//   // ==============================
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private clientFournisseurService: ClientfournisseurService,
//     private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
//     private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
//     private commandeClientFournisseurService: CommandeclientfournisseurService
//   ) { }
//
//
//   // ==============================
//   // INITIALISATION
//   // ==============================
//   ngOnInit(): void {
//     /**
//      * Récupération de l’origine (client/fournisseur)
//      */
//     this.activatedRoute.data.subscribe(data => {
//       this.origin = data['origin'];
//       console.log('ORIGIN =', this.origin);
//     });
//     this.findClientFournisseur();
//
//     this.categoryService.findAll()
//       .subscribe(categories => {
//         this.listeCategorie = categories;
//       });
//     const idArticle = this.activatedRoute.snapshot.params['idArticle'];
//     if(idArticle){
//       this.articleService.findArticleById(idArticle)
//         .subscribe(article =>{
//           this.articleDto = article;
//           this.categorieDto = this.articleDto.category ? this.articleDto.category : {};
//         });
//     }
//
//     // Chargement des données initiales
//     this.findAllClientsFournisseurs();
//     this.findAllArticles();
//
//     /**
//      * Vérifie si on est en mode modification
//      * (présence d’un id dans l’URL)
//      */
//     const idCommande = this.activatedRoute.snapshot.params['idCommande'];
//     if (idCommande) {
//       this.chargerCommandePourModification(idCommande);
//     }
//   }
//
//
//   findClientFournisseur(): void{   //methode permettant de trouver soit le client soit le fournisseur
//     const id = this.activatedRoute.snapshot.params['id'];
//     if (id) {
//       if (this.origin === 'client') {
//         this.clientFournisseurService.findClientById(id)
//           .subscribe(client => {
//             this.clientFournisseur = client;
//           });
//       } else if (this.origin === 'fournisseur') {
//         this.clientFournisseurService.findFournisseurById(id)
//           .subscribe(fournisseur => {
//             this.clientFournisseur = fournisseur;
//           });
//       }
//     }
//   }
//
//   // ==============================
//   // CHARGEMENT D’UNE COMMANDE (MODE EDIT)
//   // ==============================
//   chargerCommandePourModification(id: number): void {
//     if (this.origin === 'client') {
//       // 1. Charger les lignes de la commande
//       this.commandeClientFournisseurService.findAllLigneCommandesClient(id)
//         .subscribe(lignes => {
//           this.lignesCommande = lignes;
//           this.calculerTotalCommande();
//         });
//
//       // 2. Charger les infos générales (Code commande et Client)
//       this.commandeClientFournisseurService.findCommandeClientById(id)
//         .subscribe(cmd => {
//           this.codeCommande = cmd.code || ''; // Affiche le CODE COMMANDE
//           this.selectedClientFournisseur = cmd.client || {}; // Affiche les infos CLIENT
//         });
//
//     } else if (this.origin === 'fournisseur') {
//       // 1. Charger les lignes
//       this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(id)
//         .subscribe(lignes => {
//           this.lignesCommande = lignes;
//           this.calculerTotalCommande();
//         });
//
//       // 2. Charger les infos générales
//       this.commandeClientFournisseurService.findCommandeFournisseurById(id)
//         .subscribe(cmd => {
//           this.codeCommande = cmd.code || ''; // Affiche le CODE COMMANDE
//           this.selectedClientFournisseur = cmd.fournisseur || {}; // Affiche les infos FOURNISSEUR
//         });
//     }
//   }
//
//
//   // ==============================
//   // ANNULATION / RETOUR
//   // ==============================
//   cancelClick(): void {
//
//     if (this.origin === 'client') {
//       this.router.navigate(['commandesclient']);
//     } else if (this.origin === 'fournisseur') {
//       this.router.navigate(['commandesfournisseur']);
//     }
//   }
//
//
//   // ==============================
//   // CHARGEMENT CLIENTS / FOURNISSEURS
//   // ==============================
//   findAllClientsFournisseurs(): void {
//
//     if (this.origin === 'client') {
//
//       this.clientFournisseurService.findAllClients()
//         .subscribe(clients => {
//           this.listClientsFournisseurs = clients;
//         });
//
//     } else if (this.origin === 'fournisseur') {
//
//       this.clientFournisseurService.findAllFournisseurs()
//         .subscribe(fournisseurs => {
//           this.listClientsFournisseurs = fournisseurs;
//         });
//     }
//   }
//
//
//
//   // ==============================
//   // CHARGEMENT ARTICLES
//   // ==============================
//   findAllArticles(): void {
//
//     this.articleService.findAllArticle()
//       .subscribe(articles => {
//         this.listArticle = articles;
//       });
//   }
//
//
//   // ==============================
//   // RECHERCHE ARTICLE PAR CODE
//   // ==============================
//   findArticleByCode(codeArticle: string): void {
//
//     this.articleErrorMsg = '';
//
//     if (codeArticle) {
//       this.articleService.findArticleByCode(codeArticle)
//         .subscribe(article => {
//           this.searchedArticle = article;
//         }, error => {
//           this.articleErrorMsg = error.error.message;
//         });
//     }
//   }
//
//
//   // ==============================
//   // FILTRAGE DES ARTICLES
//   // ==============================
//   filtrerArticle(): void {
//
//     if (this.codeArticle.length === 0) {
//       this.findAllArticles();
//     }
//
//     this.listArticle = this.listArticle.filter(art =>
//       art.codeArticle?.includes(this.codeArticle) ||
//       art.designation?.includes(this.codeArticle)
//     );
//   }
//
//
//   // ==============================
//   // AJOUT LIGNE COMMANDE
//   // ==============================
//   ajouterLigneCommande(): void {
//
//     this.checkLigneCommande();
//
//     // Recalcul du total
//     this.calculerTotalCommande();
//
//     // Reset champs
//     this.searchedArticle = {};
//     this.quantite = '';
//     this.codeArticle = '';
//     this.articleNotYetSelected = false;
//
//     this.findAllArticles();
//   }
//
//
//   // ==============================
//   // CALCUL TOTAL COMMANDE
//   // ==============================
//   calculerTotalCommande(): void {
//
//     this.totalCommande = 0;
//
//     this.lignesCommande.forEach(ligne => {
//       if (ligne.prixUnitaire && ligne.quantite) {
//         this.totalCommande += +ligne.prixUnitaire * +ligne.quantite;
//       }
//     });
//   }
//
//
//   // ==============================
//   // VERIFICATION / AJOUT LIGNE
//   // ==============================
//   private checkLigneCommande(): void {
//
//     const ligneExistante = this.lignesCommande
//       .find(lig => lig.article?.codeArticle === this.searchedArticle.codeArticle);
//
//     if (ligneExistante) {
//
//       // Si l'article existe déjà → on augmente la quantité
//       this.lignesCommande.forEach(lig => {
//         if (lig.article?.codeArticle === this.searchedArticle.codeArticle) {
//           lig.quantite = lig.quantite + +this.quantite;
//         }
//       });
//
//     } else {
//
//       // Sinon → création d’une nouvelle ligne
//       const ligneCmd: LigneCommandeClientDto = {
//         article: this.searchedArticle,
//         prixUnitaire: this.searchedArticle.prixUnitaireTtc,
//         quantite: +this.quantite
//       };
//
//       this.lignesCommande.push(ligneCmd);
//     }
//   }
//
//
//   // ==============================
//   // SELECTION ARTICLE
//   // ==============================
//   selectArticleClick(article: ArticleDto): void {
//
//     this.searchedArticle = article;
//     this.codeArticle = article.codeArticle || '';
//     this.articleNotYetSelected = true;
//   }
//
//
//   // ==============================
//   // ENREGISTREMENT COMMANDE
//   // ==============================
//   enregistrerCommande(): void {
//
//     const commande = this.preparerCommande();
//
//     if (this.origin === 'client') {
//
//       this.commandeClientFournisseurService.enregistrerCommandeClient(commande as CommandeClientDto)
//         .subscribe(() => {
//           this.router.navigate(['commandesclient']);
//         }, error => this.handleError(error));
//
//     } else if (this.origin === 'fournisseur') {
//
//       this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande as CommandeFournisseurDto)
//         .subscribe(() => {
//           this.router.navigate(['commandesfournisseur']);
//         }, error => this.handleError(error));
//     }
//   }
//
//
//   // ==============================
//   // PREPARATION OBJET COMMANDE
//   // ==============================
//   private preparerCommande(): any {
//
//     if (this.origin === 'client') {
//
//       return {
//         client: this.selectedClientFournisseur,
//         code: this.codeCommande,
//         dateCommande: new Date().getTime(),
//         etatCommande: 'EN_PREPARATION',
//         ligneCommandeClients: this.lignesCommande
//       };
//
//     } else if (this.origin === 'fournisseur') {
//
//       return {
//         fournisseur: this.selectedClientFournisseur,
//         code: this.codeCommande,
//         dateCommande: new Date().getTime(),
//         etatCommande: 'EN_PREPARATION',
//         ligneCommandeFournisseurs: this.lignesCommande
//       };
//     }
//   }
//
//
//   // ==============================
//   // GESTION ERREURS
//   // ==============================
//   private handleError(error: any): void {
//
//     if (error.error instanceof Blob) {
//
//       error.error.text().then((text: string) => {
//         const err = JSON.parse(text);
//         this.errorMsg = err.errors?.length ? err.errors : [err.message];
//       });
//
//     } else {
//
//       this.errorMsg = error.error?.errors?.length
//         ? error.error.errors
//         : [error.error?.message ?? 'Erreur inconnue'];
//     }
//   }
//
// }







// import { Component, OnInit } from '@angular/core';
// import {ActivatedRoute, Router} from "@angular/router";
// import {
//   ArticleDto,
//   CommandeClientDto,
//   CommandeFournisseurDto,
//   LigneCommandeClientDto
// } from "../../../gs-api/src";
// import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";
// import {ArticleService} from "../../services/article/article.service";
// import {
//   CommandeclientfournisseurService
// } from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";
//
// @Component({
//   selector: 'app-nouvelle-commande-client-fournisseur',
//   templateUrl: './nouvelle-commande-client-fournisseur.component.html',
//   styleUrls: ['./nouvelle-commande-client-fournisseur.component.scss']
// })
// export class NouvelleCommandeClientFournisseurComponent implements OnInit {
//
//   origin = '';
//   selectedClientFournisseur: any = {};
//   // selectedClientFournisseur: ClientDto = {};
//   listClientsFournisseurs: Array<any> = [];
//   searchedArticle: ArticleDto = {};
//   listArticle: Array<ArticleDto> = [];
//   articleErrorMsg = '';
//   codeArticle = '';
//   quantite = '';
//   codeCommande = '';
//
//   lignesCommande: Array<any> = [];
//   totalCommande = 0 ;
//   articleNotYetSelected = false;
//   errorMsg: Array<string> = [];
//   user: any;
//
//   constructor(
//     private router: Router,
//     private activatedRoute: ActivatedRoute,
//     private clientFournisseurService:ClientfournisseurService,
//     private articleService: ArticleService,
//     private commandeClientFournisseurService: CommandeclientfournisseurService
//   ) { }
//
//
//   ngOnInit(): void {
//     this.activatedRoute.data.subscribe(data =>{
//       this.origin = data['origin'];
//     });
//     this.findAllClientsFournisseurs();
//     this.findAllArticles();
//
//     // AJOUTEZ CECI : Récupération de l'ID depuis l'URL
//     const idCommande = this.activatedRoute.snapshot.params['idCommande'];
//     if (idCommande) {
//       this.chargerCommandePourModification(idCommande);
//     }
//   }
//
//
//   chargerCommandePourModification(id: number): void {
//     if (this.origin === 'client') {
//       this.commandeClientFournisseurService.findAllLigneCommandesClient(id)
//         .subscribe(lignes => {
//           this.lignesCommande = lignes;
//           this.calculerTotalCommande();
//         });
//       // Vous devez aussi charger les détails de la commande (code, client, etc.)
//       // via une méthode findById dans votre service
//       this.commandeClientFournisseurService.findCommandeClientById(id)
//         .subscribe(cmd => {
//           this.codeCommande = cmd.code!;
//           this.selectedClientFournisseur = cmd.client!;
//         });
//     } else if (this.origin === 'fournisseur') {
//       this.commandeClientFournisseurService.findAllLigneCommandesFournisseur(id)
//         .subscribe(lignes => {
//           this.lignesCommande = lignes;
//           this.calculerTotalCommande();
//         });
//       this.commandeClientFournisseurService.findCommandeFournisseurById(id)
//         .subscribe(cmd => {
//           this.codeCommande = cmd.code!;
//           this.selectedClientFournisseur = cmd.fournisseur!;
//         });
//     }
//   }
//
//
//   cancelClick(): void{
//     if(this.origin === 'client'){
//       this.router.navigate(['commandesclient']);
//     } else if(this.origin === 'fournisseur'){
//       this.router.navigate(['commandesfournisseur']);
//     }
//   }
//
//
//   findAllClientsFournisseurs(): void {
//     if (this.origin === 'client') {
//       this.clientFournisseurService.findAllClients()
//         .subscribe(clients => {
//           this.listClientsFournisseurs = clients;
//         });
//     } else if (this.origin === 'fournisseur' ) {
//       this.clientFournisseurService.findAllFournisseurs()
//         .subscribe(fournisseurs => {
//           this.listClientsFournisseurs = fournisseurs;
//         });
//     }
//   }
//
//
//   findAllArticles(): void{
//     this.articleService.findAllArticle()
//       .subscribe(articles =>{
//         this.listArticle = articles;
//       });
//   }
//
//
//   findArticleByCode(codeArticle: string): void{
//     this.articleErrorMsg = '';
//     if(codeArticle){
//       this.articleService.findArticleByCode(codeArticle)
//         .subscribe(article =>{
//           this.searchedArticle = article;
//         }, error=> {
//           this.articleErrorMsg = error.error.message;
//         });
//     }
//   }
//
//
//   filtrerArticle(): void {
//     if(this.codeArticle.length === 0){
//       this.findAllArticles()
//     }
//     this.listArticle = this.listArticle
//       .filter(art => art.codeArticle?.includes(this.codeArticle) || art.designation?.includes(this.codeArticle));
//   }
//
//
//   ajouterLigneCommande(): void {
//     this.checkLigneCommande();
//     this.calculerTotalCommande()
//
//     this.searchedArticle = {};
//     this.quantite = '';
//     this.codeArticle = '';
//     this.articleNotYetSelected = false;  //je dois pouvoir sélectionner à nouveau mon article
//     this.findAllArticles();
//   }
//
//
//   calculerTotalCommande(): void{
//     this.totalCommande = 0 ;
//     this.lignesCommande.forEach(ligne => {
//       if(ligne.prixUnitaire && ligne.quantite){
//         this.totalCommande += +ligne.prixUnitaire * +ligne.quantite;
//       }
//     });
//   }
//
//
//   private checkLigneCommande(): void{
//     const ligneCommandeAlreadyExists = this.lignesCommande
//       .find(lig => lig.article?.codeArticle === this.searchedArticle.codeArticle);
//     if (ligneCommandeAlreadyExists){
//       this.lignesCommande.forEach(lig => {
//         if(lig && lig.article?.codeArticle === this.searchedArticle.codeArticle){
//           //@ts-ignore
//           lig.quantite = lig.quantite + +this.quantite;
//         }
//       });
//     } else {
//
//       const ligneCmd: LigneCommandeClientDto = {  //je cré l'objet ligne de commande
//         article: this.searchedArticle,
//         prixUnitaire: this.searchedArticle.prixUnitaireTtc,
//         quantite: +this.quantite
//       };
//       this.lignesCommande.push(ligneCmd);
//     }
//   }
//
//
//   selectArticleClick(article: ArticleDto): void {
//     this.searchedArticle = article;
//     this.codeArticle = article.codeArticle ? article.codeArticle : '';
//     this.articleNotYetSelected = true;
//   }
//
//
//   enregistrerCommande(): void {
//     const commande = this.preparerCommande();
//     if (this.origin === 'client') {
//       this.commandeClientFournisseurService.enregistrerCommandeClient(commande as CommandeClientDto)
//         .subscribe(cmd => {
//           this.router.navigate(['commandesclient'])
//         }, error => {
//           if (error.error instanceof Blob) {
//             error.error.text().then((text: string) => {
//               const err = JSON.parse(text);
//               this.errorMsg = err.errors?.length ? err.errors : [err.message];
//             });
//           } else {
//             this.errorMsg = error.error?.errors?.length
//               ? error.error.errors
//               : [error.error?.message ?? 'Erreur inconnue'];
//           }
//         });
//     } else if (this.origin === 'fournisseur') {
//       this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande as CommandeFournisseurDto)
//         .subscribe(cmd => {
//           this.router.navigate(['commandesfournisseur'])
//         }, error => {
//           if (error.error instanceof Blob) {
//             error.error.text().then((text: string) => {
//               const err = JSON.parse(text);
//               this.errorMsg = err.errors?.length ? err.errors : [err.message];
//             });
//           } else {
//             this.errorMsg = error.error?.errors?.length
//               ? error.error.errors
//               : [error.error?.message ?? 'Erreur inconnue'];
//           }
//         });
//     }
//   }
//
//
//   private preparerCommande(): any {
//     if (this.origin === 'client') {
//       return  {
//         client: this.selectedClientFournisseur,
//         code: this.codeCommande,
//         dateCommande: new Date().getTime(),
//         etatCommande: 'EN_PREPARATION',
//
//         ligneCommandeClients: this.lignesCommande
//
//       };
//     } else if (this.origin === 'fournisseur') {
//       return  {
//         fournisseur: this.selectedClientFournisseur,
//         code: this.codeCommande,
//         dateCommande: new Date().getTime(),
//         etatCommande: 'EN_PREPARATION',
//         ligneCommandeFournisseurs: this.lignesCommande
//       };
//     }
//   }
// }

