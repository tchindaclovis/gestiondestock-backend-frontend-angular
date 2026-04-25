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
  LigneCommandeClientDto, UtilisateurDto
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
import {UserService} from "../../services/user/user.service";
import {Observable} from "rxjs";

// ==============================
// DECORATEUR COMPONENT
// ==============================
@Component({
  selector: 'app-nouvelle-commande-client-fournisseur',
  templateUrl: './nouvelle-commande-client-fournisseur.component.html',
  styleUrls: ['./nouvelle-commande-client-fournisseur.component.scss']
})

export class NouvelleCommandeClientFournisseurComponent implements OnInit {

  @Input() origin = '';
  connectedUser: UtilisateurDto | null = null;
  clientFournisseur: any = {}; //soit client soit fournisseur

  codeArticle = '';
  quantite = '';
  codeCommandeClient = ''; // Lié au champ "Code commande client"
  codeCommandeFournisseur = ''; // Lié au champ "Code commande fournisseur"
  idCommandeClient: number | null = null;
  idCommandeFournisseur: number | null = null;

  selectedClientFournisseur: any = {}; // Variable utilisée dans le HTML
  listClients: ClientDto[] = [];
  listFournisseurs: FournisseurDto[] = [];

  searchedArticle: ArticleDto = {};
  listArticle: Array<ArticleDto> = [];

  commandeClientDto: CommandeFournisseurDto = {}; //objet ou variable initialisé à vide
  commandeFournisseurDto: CommandeClientDto = {}; //objet ou variable initialisé à vide

  articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
  errorMsg : Array<string> = [];
  // listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau

  listeLignesCommande: Array<any> = [];
  totalCommande = 0;
  articleNotYetSelected = false;

  constructor(
    private router: Router,
    private userService: UserService,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
    private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
    private commandeClientFournisseurService: CommandeclientfournisseurService
  ) { }


    ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();

    // 1. Déterminer l'origine
    this.activatedRoute.data.subscribe(data => {
      this.origin = data['origin'];

      // Charger la liste correspondante dès qu'on connaît l'origine
      this.findAllClientsFournisseurs();

      // 2. Charger les articles pour l'autocomplétion
      this.findAllArticles();

      if(this.origin === 'client'){
        // 3. Mode Modification / Visualisation
        // On récupère l'ID passé dans l'URL (le "103" de votre exemple)
        const id = this.activatedRoute.snapshot.params['idCommandeClient'];
        if (id) {
          this.idCommandeClient = id;
          this.chargerDonneesPourModification(id);
          this.commandeClientFournisseurService.findCommandeClientById(id)
            .subscribe(commande =>{
              this.commandeClientDto = commande;
            });
        }else{
          this.commandeClientFournisseurService.getLastCodeCommandeClient().subscribe({
            next: async (res: any) => { // Ajoutez 'async' ici
              let rawValue = res;
              // Si la réponse est un Blob, on extrait son contenu textuel
              if (res instanceof Blob) {
                rawValue = await res.text();
              }
              console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "CMC0013"
              this.codeCommandeClient= this.genererProchainCode(rawValue);
            },
            error: (err) => {
              console.error('Erreur API :', err);
              this.codeCommandeClient = 'CMC0001';
            }
          });
        }
      }else if(this.origin === 'fournisseur'){
        // 3. Mode Modification / Visualisation
        // On récupère l'ID passé dans l'URL (le "103" de votre exemple)
        const id = this.activatedRoute.snapshot.params['idCommandeFournisseur'];
        if (id) {
          this.idCommandeClient = id;
          this.chargerDonneesPourModification(id);
          this.commandeClientFournisseurService.findCommandeFournisseurById(id)
            .subscribe(commande =>{
              this.commandeClientDto = commande;
            });
        }else{
          this.commandeClientFournisseurService.getLastCodeCommandeFournisseur().subscribe({
            next: async (res: any) => { // Ajoutez 'async' ici
              let rawValue = res;
              // Si la réponse est un Blob, on extrait son contenu textuel
              if (res instanceof Blob) {
                rawValue = await res.text();
              }
              console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "CMF0013"
              this.codeCommandeFournisseur= this.genererProchainCode(rawValue);
            },
            error: (err) => {
              console.error('Erreur API :', err);
              this.codeCommandeFournisseur = 'CMF0001';
            }
          });
        }
      }
    });
  }



  private genererProchainCode(lastCode: any): string {
    console.log('Type de lastCode :', typeof lastCode);
    console.log('Valeur brute de lastCode :', lastCode);
    // 1. Conversion en string et nettoyage radical (supprime guillemets, espaces, retours à la ligne)
    const cleanCode = String(lastCode).replace(/["\s\n\r]/g, '');

    // 2. Extraction de TOUS les chiffres présents dans la chaîne
    // On cherche une suite de chiffres (\d+)
    const match = cleanCode.match(/\d+/);

    let nextNumber = 9999; // Valeur par défaut si aucun chiffre n'est trouvé

    if (match && match[0]) {
      // 3. Conversion de la partie trouvée (ex: "0013") en nombre et incrémentation
      nextNumber = parseInt(match[0], 10) + 1;
    }

    // 4. Formatage : "CMC or CMF" + nombre formaté sur 4 positions (Milliers, Centaines, Dizaines, Unités)
    // padStart(4, '0') transforme 14 en "0014"
    const formattedNumber = nextNumber.toString().padStart(4, '0');

    return (this.origin === 'client')?`CMC${formattedNumber}`:`CMF${formattedNumber}`;
  }



  chargerDonneesPourModification(id: number): void {
    console.log("ID détecté :", id, " | Origine :", this.origin);

    // On définit l'appel dynamiquement
    const serviceCall = (this.origin === 'client') ?
      this.commandeClientFournisseurService.findCommandeClientById(id) :
      this.commandeClientFournisseurService.findCommandeFournisseurById(id);

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
    if (this.origin === 'client') {
      this.idCommandeClient = cmd.id; // On stocke l'ID reçu
      this.codeCommandeClient = cmd.code || '';
      this.selectedClientFournisseur = cmd.client; // On récupère soit le client selon l'origine
      console.log("Données affectées au formulaire :", this.codeCommandeClient);
    } else if(this.origin === 'fournisseur') {
      this.idCommandeFournisseur = cmd.id; // On stocke l'ID reçu
      this.codeCommandeFournisseur = cmd.code || '';
      this.selectedClientFournisseur = cmd.fournisseur; // On récupère soit le fournisseur selon l'origine
      console.log("Données affectées au formulaire :", this.codeCommandeFournisseur);
    }
  }



  private chargerLignesCommande(idCmd: number): void {
    // 1. Déclaration avec initialisation pour éviter l'erreur "used before being assigned"
    let serviceLignes: Observable<any>;

      serviceLignes = (this.origin === 'client') ?
        this.commandeClientFournisseurService.findAllLigneCommandesClientByCommande(idCmd) :
        this.commandeClientFournisseurService.findAllLigneCommandesFournisseurByCommande(idCmd);

    // 2. Appel du subscribe
    serviceLignes.subscribe({
      next: (res) => {
        if (res instanceof Blob) {
          // Utilisation de async/await ou .then pour extraire le texte du Blob
          res.text().then(text => {
            try {
              this.listeLignesCommande = JSON.parse(text);
              this.calculerTotalCommande();
            } catch (e) {
              console.error('Erreur de parsing JSON du Blob', e);
            }
          });
        } else {
          this.listeLignesCommande = res;
          this.calculerTotalCommande();
        }
        console.log("Lignes de vente :", this.listeLignesCommande);
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des lignes', err);
      }
    });
  }


  calculerTotalCommande(): void {
    this.totalCommande = 0;
    this.listeLignesCommande.forEach(lig => {
        // On récupère le prix disponible
        const prix = (this.origin === 'client') ?
          lig.prixVenteUnitaireTtc :
          lig.prixUnitaireTtc;

        this.totalCommande += (+prix * +lig.quantite);
    });
  }


  // ==============================
  // LOGIQUE METIER (Correction des liaisons)
  // ==============================

  compareFn(c1: any, c2: any): boolean {
    return c1 && c2 ? c1.id === c2.id : c1 === c2;
  }


    findAllClientsFournisseurs(): void {
    const idEntreprise = this.connectedUser?.entreprise?.id;
    if (idEntreprise) {
      if (this.origin === 'client') {
        this.clientFournisseurService.findAllClientByIdEntreprise(idEntreprise)
          .subscribe(res => {
            this.listClients = res;
          });
      } else if (this.origin === 'fournisseur') {
        this.clientFournisseurService.findAllFournisseurs()
          .subscribe(res => {
            this.listFournisseurs = res;
          });
      }
    }
  }


  findAllArticles(): void {
    // On récupère l'id de l'entreprise de l'utilisateur connecté
    const idEntreprise = this.connectedUser?.entreprise?.id;
    if (idEntreprise) {
      this.articleService.findAllArticlesByIdEntreprise(idEntreprise)
        .subscribe(res => this.listArticle = res);
    }
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

  selectArticleClick(articleDto: ArticleDto): void {
    this.searchedArticle = articleDto;
    this.codeArticle = articleDto.codeArticle || '';
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
    // On cherche si l'article est déjà présent dans la liste actuelle
    const ligneExistante = this.listeLignesCommande.find(lig =>
      lig.article?.id === this.searchedArticle.id
    );

    if (ligneExistante) {
      // Si elle existe, on met à jour la quantité sur l'objet existant (qui a déjà un ID de la BDD)
      ligneExistante.quantite = Number(ligneExistante.quantite) + Number(this.quantite);
    } else {
      // Si c'est un nouvel article, on crée une nouvelle ligne (id sera null)
      const nouvelleLigne: any = {
        article: this.searchedArticle,
        quantite: Number(this.quantite),
        idEntreprise: this.connectedUser?.entreprise?.id
      };

      if (this.origin === 'client') {
        nouvelleLigne.prixVenteUnitaireTtc = this.searchedArticle.prixVenteUnitaireTtc;
      } else if (this.origin === 'fournisseur') {
        nouvelleLigne.prixUnitaireTtc = this.searchedArticle.prixUnitaireTtc;
      }

      this.listeLignesCommande.push(nouvelleLigne);
    }
  }


  enregistrerCommande(): void {
    const commande = this.preparerCommande();
    if (this.origin === 'client') {
      this.commandeClientFournisseurService.enregistrerCommandeClient(commande).subscribe({
        next:() =>{
          this.router.navigate(['commandesclient']);
      },
          error: (e) => this.handleError(e)
      });
    } else if (this.origin === 'fournisseur') {
      this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande).subscribe({
        next: () => {
          this.router.navigate(['commandesfournisseur'])
        },
        error: (e) => this.handleError(e)
      });
    }
  }


  private preparerCommande(): any {
    const idEnt = this.connectedUser?.entreprise?.id;
    // Utiliser l'ID général récupéré lors du ngOnInit (idCommande)
    const currentId = (this.origin === 'client') ? this.idCommandeClient : this.idCommandeFournisseur;

    const lignesPourBackend = this.listeLignesCommande.map(ligne => {
      return {
        id: ligne.id || null, // CRITIQUE : Garder l'ID de la ligne existante
        article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
        quantite: ligne.quantite,
        // On affecte le bon prix selon le contexte
        prixVenteUnitaireTtc: (this.origin === 'client') ? ligne.prixVenteUnitaireTtc : undefined,
        prixUnitaireTtc: (this.origin === 'fournisseur') ? ligne.prixUnitaireTtc : undefined,
        idEntreprise: idEnt
      };
    });

    return {
      id: currentId, // Si présent, Hibernate fera un UPDATE // CRITIQUE : L'ID de la commande pour déclencher l'UPDATE au lieu du INSERT
      [this.origin]: this.selectedClientFournisseur,
      // client: { id: this.selectedClientFournisseur?.id },
      code: (this.origin === 'client') ? this.codeCommandeClient : this.codeCommandeFournisseur,
      dateCommande: new Date().toISOString(), // Utiliser ISOString pour la stabilité
      etatCommande: 'EN_PREPARATION',
      idEntreprise: idEnt,
      [this.origin === 'client' ? 'ligneCommandeClients' : 'ligneCommandeFournisseurs']: lignesPourBackend
    };
  }


  cancelClick(): void {
    this.router.navigate([(this.origin === 'client') ?
      'commandesclient' :
      'commandesfournisseur'
    ]);
  }

  private handleError(error: any): void {
    this.errorMsg = error.error?.errors || [error.error?.message || 'Erreur'];
  }
}






// // ==============================
// // IMPORTS ANGULAR
// // ==============================
// import {Component, Input, OnInit} from '@angular/core';
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
//   ArticleDto, CategoryDto, ClientDto,
//   CommandeClientDto,
//   CommandeFournisseurDto, FournisseurDto,
//   LigneCommandeClientDto, UtilisateurDto
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
// import {UserService} from "../../services/user/user.service";
// import {Observable} from "rxjs";
//
// // ==============================
// // DECORATEUR COMPONENT
// // ==============================
// @Component({
//   selector: 'app-nouvelle-commande-client-fournisseur',
//   templateUrl: './nouvelle-commande-client-fournisseur.component.html',
//   styleUrls: ['./nouvelle-commande-client-fournisseur.component.scss']
// })
//
// export class NouvelleCommandeClientFournisseurComponent implements OnInit {
//
//   @Input() origin = '';
//   connectedUser: UtilisateurDto | null = null;
//   clientFournisseur: any = {}; //soit client soit fournisseur
//
//   codeArticle = '';
//   quantite = '';
//   codeCommandeClient = ''; // Lié au champ "Code commande client"
//   codeCommandeFournisseur = ''; // Lié au champ "Code commande fournisseur"
//   idCommandeClient: number | null = null;
//   idCommandeFournisseur: number | null = null;
//   idCommande: number | null = null;
//   commande: any = {};
//
//
//   selectedClientFournisseur: any = {}; // Variable utilisée dans le HTML
//   listClients: ClientDto[] = [];
//   listFournisseurs: FournisseurDto[] = [];
//
//
//
//   searchedArticle: ArticleDto = {};
//   listArticle: Array<ArticleDto> = [];
//
//   commandeClientDto: CommandeFournisseurDto = {}; //objet ou variable initialisé à vide
//   commandeFournisseurDto: CommandeClientDto = {}; //objet ou variable initialisé à vide
//
//   articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
//   errorMsg : Array<string> = [];
//   // listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau
//
//   listeLignesCommande: Array<any> = [];
//   totalCommande = 0;
//   articleNotYetSelected = false;
//
//   constructor(
//     private router: Router,
//     private userService: UserService,
//     private activatedRoute: ActivatedRoute,
//     private clientFournisseurService: ClientfournisseurService,
//     private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
//     private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
//     private commandeClientFournisseurService: CommandeclientfournisseurService
//   ) { }
//
//   ngOnInit(): void {
//     // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
//     this.connectedUser = this.userService.getConnectedUser();
//
//     // Récupération de l'ID depuis l'URL (ex: /nouvellevente/253)
//     const id = this.activatedRoute.snapshot.params['idCommande'];
//     if (id) {
//       this.idCommande = id;
//       // ... votre logique pour charger la vente existante ...
//     }
//
//     // 1. Déterminer l'origine
//     this.activatedRoute.data.subscribe(data => {
//       this.origin = data['origin'];
//
//       // Charger la liste correspondante dès qu'on connaît l'origine
//       this.findAllClientsFournisseurs();
//
//       // 2. Charger les articles pour l'autocomplétion
//       this.findAllArticles();
//
//       // 3. Mode Modification / Visualisation
//       // On récupère l'ID passé dans l'URL (le "103" de votre exemple)
//       const id = this.activatedRoute.snapshot.params['idCommande'];
//
//       if(id){
//         this.chargerDonneesPourModification(id);
//         if(this.origin === 'client') {
//           this.commandeClientFournisseurService.findCommandeClientById(id)
//             .subscribe(commande => {
//               this.commandeClientDto= commande;
//             });
//         }else{
//           this.commandeClientFournisseurService.findCommandeFournisseurById(id)
//             .subscribe(commande => {
//               this.commandeFournisseurDto = commande;
//             });
//         }
//       }else {
//         if(this.origin === 'client') {
//           this.commandeClientFournisseurService.getLastCodeCommandeClient()
//             .subscribe({
//               next: async (res: any) => { // Ajoutez 'async' ici
//                 let rawValue = res;
//
//                 // Si la réponse est un Blob, on extrait son contenu textuel
//                 if (res instanceof Blob) {
//                   rawValue = await res.text();
//                 }
//
//                 console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "CMC0013"
//                 this.codeCommandeClient = this.genererProchainCode(rawValue);
//               },
//               error: (err) => {
//                 console.error('Erreur API :', err);
//                 this.codeCommandeClient = 'CMC0001';
//               }
//             });
//         }else{
//           this.commandeClientFournisseurService.getLastCodeCommandeFournisseur().subscribe({
//             next: async (res: any) => { // Ajoutez 'async' ici
//               let rawValue = res;
//
//               // Si la réponse est un Blob, on extrait son contenu textuel
//               if (res instanceof Blob) {
//                 rawValue = await res.text();
//               }
//
//               console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "CMC0013"
//               this.codeCommandeFournisseur = this.genererProchainCode(rawValue);
//             },
//             error: (err) => {
//               console.error('Erreur API :', err);
//               this.codeCommandeFournisseur ='CMF0001';
//             }
//           });
//         }
//       }
//     });
//   }
//
//
//   // ngOnInit(): void {
//   //   // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
//   //   this.connectedUser = this.userService.getConnectedUser();
//   //
//   //   // 1. Déterminer l'origine
//   //   this.activatedRoute.data.subscribe(data => {
//   //     this.origin = data['origin'];
//   //     if(this.origin === 'client'){
//   //       // Récupération de l'ID depuis l'URL (ex: /nouvellecommandeclient/253)
//   //       const idCommandeClient = this.activatedRoute.snapshot.params['idCommandeClient'];
//   //       if (idCommandeClient) {
//   //         this.idCommandeClient = idCommandeClient;
//   //         // ... votre logique pour charger la vente existante ...
//   //       }
//   //
//   //       // Charger la liste correspondante dès qu'on connaît l'origine
//   //       this.findAllClientsFournisseurs();
//   //
//   //       // 2. Charger les articles pour l'autocomplétion
//   //       this.findAllArticles();
//   //
//   //       // 3. Mode Modification / Visualisation
//   //       // On récupère l'ID passé dans l'URL (le "103" de votre exemple)
//   //       const id = this.activatedRoute.snapshot.params['idCommandeClient'];
//   //       if (id) {
//   //         this.chargerDonneesPourModification(id);
//   //         this.commandeClientFournisseurService.findCommandeClientById(id)
//   //           .subscribe(commandeclient =>{
//   //             this.commandeClientDto = commandeclient;
//   //           });
//   //       }else{
//   //         this.commandeClientFournisseurService.getLastCodeCommandeClient().subscribe({
//   //           next: async (res: any) => { // Ajoutez 'async' ici
//   //             let rawValue = res;
//   //             // Si la réponse est un Blob, on extrait son contenu textuel
//   //             if (res instanceof Blob) {
//   //               rawValue = await res.text();
//   //             }
//   //             console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "CMC0013"
//   //             this.codeCommandeClient= this.genererProchainCode(rawValue);
//   //           },
//   //           error: (err) => {
//   //             console.error('Erreur API :', err);
//   //             this.codeCommandeClient = 'CMC0001';
//   //           }
//   //         });
//   //       }
//   //     }else if(this.origin === 'fournisseur'){
//   //       // Récupération de l'ID depuis l'URL (ex: /nouvellecommandefournisseur/253)
//   //       const idCommandeFournisseur = this.activatedRoute.snapshot.params['idCommandeFournisseur'];
//   //       if (idCommandeFournisseur) {
//   //         this.idCommandeClient = idCommandeFournisseur;
//   //         // ... votre logique pour charger la vente existante ...
//   //       }
//   //
//   //       // Charger la liste correspondante dès qu'on connaît l'origine
//   //       this.findAllClientsFournisseurs();
//   //
//   //       // 2. Charger les articles pour l'autocomplétion
//   //       this.findAllArticles();
//   //
//   //       // 3. Mode Modification / Visualisation
//   //       // On récupère l'ID passé dans l'URL (le "103" de votre exemple)
//   //       const id = this.activatedRoute.snapshot.params['idCommandeFournisseur'];
//   //       if (id) {
//   //         this.chargerDonneesPourModification(id);
//   //         this.commandeClientFournisseurService.findCommandeFournisseurById(id)
//   //           .subscribe(commandefournisseur =>{
//   //             this.commandeClientDto = commandefournisseur;
//   //           });
//   //       }else{
//   //         this.commandeClientFournisseurService.getLastCodeCommandeFournisseur().subscribe({
//   //           next: async (res: any) => { // Ajoutez 'async' ici
//   //             let rawValue = res;
//   //             // Si la réponse est un Blob, on extrait son contenu textuel
//   //             if (res instanceof Blob) {
//   //               rawValue = await res.text();
//   //             }
//   //             console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "CMF0013"
//   //             this.codeCommandeFournisseur= this.genererProchainCode(rawValue);
//   //           },
//   //           error: (err) => {
//   //             console.error('Erreur API :', err);
//   //             this.codeCommandeFournisseur = 'CMF0001';
//   //           }
//   //         });
//   //       }
//   //     }
//   //   });
//   // }
//
//
//
//   private genererProchainCode(lastCode: any): string {
//     console.log('Type de lastCode :', typeof lastCode);
//     console.log('Valeur brute de lastCode :', lastCode);
//     // 1. Conversion en string et nettoyage radical (supprime guillemets, espaces, retours à la ligne)
//     const cleanCode = String(lastCode).replace(/["\s\n\r]/g, '');
//
//     // 2. Extraction de TOUS les chiffres présents dans la chaîne
//     // On cherche une suite de chiffres (\d+)
//     const match = cleanCode.match(/\d+/);
//
//     let nextNumber = 9999; // Valeur par défaut si aucun chiffre n'est trouvé
//
//     if (match && match[0]) {
//       // 3. Conversion de la partie trouvée (ex: "0013") en nombre et incrémentation
//       nextNumber = parseInt(match[0], 10) + 1;
//     }
//
//     // 4. Formatage : "CMC or CMF" + nombre formaté sur 4 positions (Milliers, Centaines, Dizaines, Unités)
//     // padStart(4, '0') transforme 14 en "0014"
//     const formattedNumber = nextNumber.toString().padStart(4, '0');
//
//     return (this.origin === 'client')?`CMC${formattedNumber}`:`CMF${formattedNumber}`;
//   }
//
//
//
//   chargerDonneesPourModification(id: number): void {
//     console.log("ID détecté :", id, " | Origine :", this.origin);
//
//     // On définit l'appel dynamiquement
//     const serviceCall = (this.origin === 'client') ?
//       this.commandeClientFournisseurService.findCommandeClientById(id) :
//       this.commandeClientFournisseurService.findCommandeFournisseurById(id);
//
//     // On force le type à 'any' pour le subscribe pour éviter l'erreur TS2349
//     (serviceCall as any).subscribe((res: any) => {
//       this.traiterReponse(res, id);
//     }, (error: any) => {
//       this.handleError(error);
//     });
//   }
//
//   private traiterReponse(res: any, id: number): void {
//     // Cas où la réponse est un Blob (Binaire JSON)
//     if (res instanceof Blob) {
//       res.text().then(text => {
//         const cmd = JSON.parse(text);
//         this.affecterDonnees(cmd);
//         this.chargerLignesCommande(id);
//       });
//     }
//     // Cas où la réponse est déjà un objet JSON
//     else {
//       this.affecterDonnees(res);
//       this.chargerLignesCommande(id);
//     }
//   }
//
//   private affecterDonnees(cmd: any): void {
//     if(this.origin === 'client') {
//       this.codeCommandeClient = cmd.code || '';
//       // On récupère soit le client selon l'origine
//       this.selectedClientFournisseur = cmd.client;
//       console.log("Données affectées au formulaire :", this.codeCommandeClient);
//     }else{
//       this.codeCommandeFournisseur = cmd.code || '';
//       // On récupère soit le fournisseur selon l'origine
//       this.selectedClientFournisseur = cmd.fournisseur;
//
//       console.log("Données affectées au formulaire :", this.codeCommandeFournisseur);
//     }
//   }
//
//
//   private chargerLignesCommande(idCommande: number): void {
//     // 1. Déclaration avec initialisation pour éviter l'erreur "used before being assigned"
//     let serviceLignes: Observable<any>;
//
//     serviceLignes = (this.origin === 'client') ?
//       this.commandeClientFournisseurService.findAllLigneCommandesClientByCommande(idCommande) :
//       this.commandeClientFournisseurService.findAllLigneCommandesFournisseurByCommande(idCommande);
//
//     // 2. Appel du subscribe
//     serviceLignes.subscribe({
//       next: (res) => {
//         if (res instanceof Blob) {
//           // Utilisation de async/await ou .then pour extraire le texte du Blob
//           res.text().then(text => {
//             try {
//               this.listeLignesCommande = JSON.parse(text);
//               this.calculerTotalCommande();
//             } catch (e) {
//               console.error('Erreur de parsing JSON du Blob', e);
//             }
//           });
//         } else {
//           this.listeLignesCommande = res;
//           this.calculerTotalCommande();
//         }
//       },
//       error: (err) => {
//         console.error('Erreur lors de la récupération des lignes', err);
//       }
//     });
//   }
//
//
//   calculerTotalCommande(): void {
//     this.totalCommande = 0;
//     this.listeLignesCommande.forEach(lig => {
//       // On récupère le prix disponible
//       const prix = (this.origin === 'client') ?
//         lig.prixVenteUnitaireTtc :
//         lig.prixUnitaireTtc;
//
//       this.totalCommande += (+prix * +lig.quantite);
//
//       // // On récupère le prix disponible
//       // const prix = lig.prixUnitaire || lig.prixVenteUnitaireTtc || lig.prixUnitaireTtc || 0;
//       // this.totalCommande += (+prix * +lig.quantite);
//     });
//   }
//
//
//   // ==============================
//   // LOGIQUE METIER (Correction des liaisons)
//   // ==============================
//
//   compareFn(c1: any, c2: any): boolean {
//     return c1 && c2 ? c1.id === c2.id : c1 === c2;
//   }
//
//   // findAllClientsFournisseurs(): void {
//   //   const idEntreprise = this.connectedUser?.entreprise?.id;
//   //   if (idEntreprise) {
//   //     (this.origin === 'client') ?
//   //       this.clientFournisseurService.findAllClientByIdEntreprise(idEntreprise)
//   //         .subscribe(res => this.listClientsFournisseurs = res) :
//   //       this.clientFournisseurService.findAllFournisseurs()
//   //         .subscribe(res => this.listClientsFournisseurs = res);
//   //   }
//   // }
//
//
//   findAllClientsFournisseurs(): void {
//     const idEntreprise = this.connectedUser?.entreprise?.id;
//     if (idEntreprise) {
//       if (this.origin === 'client') {
//         this.clientFournisseurService.findAllClientByIdEntreprise(idEntreprise)
//           .subscribe(res => {
//             this.listClients = res;
//           });
//       } else {
//         this.clientFournisseurService.findAllFournisseurs()
//           .subscribe(res => {
//             this.listFournisseurs = res;
//           });
//       }
//     }
//   }
//
//
//   findAllArticles(): void {
//     // On récupère l'id de l'entreprise de l'utilisateur connecté
//     const idEntreprise = this.connectedUser?.entreprise?.id;
//     if (idEntreprise) {
//       this.articleService.findAllArticlesByIdEntreprise(idEntreprise)
//         .subscribe(res => this.listArticle = res);
//     }
//   }
//
//   filtrerArticle(): void {
//     if (!this.codeArticle) {
//       this.findAllArticles();
//       return;
//     }
//     this.listArticle = this.listArticle.filter(art =>
//       art.codeArticle?.toLowerCase().includes(this.codeArticle.toLowerCase()) ||
//       art.designation?.toLowerCase().includes(this.codeArticle.toLowerCase())
//     );
//   }
//
//   selectArticleClick(articleDto: ArticleDto): void {
//     this.searchedArticle = articleDto;
//     this.codeArticle = articleDto.codeArticle || '';
//     this.articleNotYetSelected = true;
//   }
//
//   ajouterLigneCommande(): void {
//     this.checkLigneCommande();
//     this.calculerTotalCommande();
//     // Reset
//     this.searchedArticle = {};
//     this.quantite = '';
//     this.codeArticle = '';
//     this.articleNotYetSelected = false;
//   }
//
//
//   private checkLigneCommande(): void {
//     const ligneExistante = this.listeLignesCommande.find(lig =>
//       lig.article?.codeArticle === this.searchedArticle.codeArticle
//     );
//
//     if (ligneExistante) {
//       ligneExistante.quantite += +this.quantite;
//     } else {
//       if (this.origin === 'client') {
//         const ligneCmd = {
//           article: this.searchedArticle,
//           prixVenteUnitaireTtc: this.searchedArticle.prixVenteUnitaireTtc,
//           quantite: +this.quantite
//         };
//         this.listeLignesCommande.push(ligneCmd);
//       }else{
//         const ligneCmd = {
//           article: this.searchedArticle,
//           prixUnitaireTtc: this.searchedArticle.prixUnitaireTtc,
//           quantite: +this.quantite
//         };
//         this.listeLignesCommande.push(ligneCmd);
//       }
//     }
//   }
//
//   enregistrerCommande(): void {
//     const commande = this.preparerCommande();
//     if (this.origin === 'client') {
//       this.commandeClientFournisseurService.enregistrerCommandeClient(commande).subscribe({
//         next:() =>{
//           this.router.navigate(['commandesclient']);
//         },
//         error: (e) => this.handleError(e)
//       });
//     } else {
//       this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande).subscribe({
//         next: () => {
//           this.router.navigate(['commandesfournisseur'])
//         },
//         error: (e) => this.handleError(e)
//       });
//     }
//   }
//
//
//   // enregistrerCommande(): void {
//   //   const commande = this.preparerCommande();
//   //   if (this.origin === 'client') {
//   //     this.commandeClientFournisseurService.enregistrerCommandeClient(commande)
//   //       .subscribe(() => this.router.navigate(['commandesclient']), e => this.handleError(e));
//   //   } else {
//   //     this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande)
//   //       .subscribe(() => this.router.navigate(['commandesfournisseur']), e => this.handleError(e));
//   //   }
//   // }
//
//
//
//   // private preparerCommande(): any {
//   //   const idEnt = this.connectedUser?.entreprise?.id;
//   //   // Utiliser l'ID général récupéré lors du ngOnInit (idCommande)
//   //   const currentId = this.idCommande;
//   //
//   //   if(this.origin === 'client') {
//   //     const lignesPourBackend = this.listeLignesCommande.map(ligne => {
//   //       return {
//   //         id: ligne.id || null, // CRITIQUE : Garder l'ID de la ligne existante
//   //         article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
//   //         quantite: ligne.quantite,
//   //         prixVenteUnitaireTtc: ligne.prixVenteUnitaireTtc,
//   //         idEntreprise: idEnt
//   //       };
//   //     });
//   //
//   //     return {
//   //       id: currentId, // CRITIQUE : L'ID de la commande pour déclencher l'UPDATE au lieu du INSERT
//   //       client: { id: this.selectedClientFournisseur?.id },
//   //       code: this.codeCommandeClient,
//   //       dateCommande: new Date().toISOString(), // Utiliser ISOString pour la stabilité
//   //       etatCommande: 'EN_PREPARATION',
//   //       idEntreprise: idEnt,
//   //       ligneCommandeClients: lignesPourBackend
//   //     };
//   //   } else {
//   //     const lignesPourBackend = this.listeLignesCommande.map(ligne => {
//   //       return {
//   //         id: ligne.id || null, // CRITIQUE
//   //         article: { id: ligne.article?.id },
//   //         quantite: ligne.quantite,
//   //         prixUnitaireTtc: ligne.prixUnitaireTtc,
//   //         idEntreprise: idEnt
//   //       };
//   //     });
//   //
//   //     return {
//   //       id: currentId, // CRITIQUE
//   //       fournisseur: { id: this.selectedClientFournisseur?.id },
//   //       code: this.codeCommandeFournisseur,
//   //       dateCommande: new Date().toISOString(),
//   //       etatCommande: 'EN_PREPARATION',
//   //       idEntreprise: idEnt,
//   //       ligneCommandeFournisseurs: lignesPourBackend
//   //     };
//   //   }
//   // }
//
//
//
//   private preparerCommande(): any {
//     const idEnt = this.connectedUser?.entreprise?.id;
//     if(this.origin === 'client') {
//       // On crée une copie des lignes pour ne pas modifier l'affichage en cours
//       const lignesPourBackend = this.listeLignesCommande.map(ligne => {
//         return {
//           // Très important : garder l'ID de la ligne si elle existe déjà
//           id: ligne.id || null,
//           article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
//           // article: ligne.article,
//           quantite: ligne.quantite,
//           prixVenteUnitaireTtc: ligne.prixVenteUnitaireTtc,
//           // [(this.origin === 'client') ? 'prixVenteUnitaireTtc' : 'prixUnitaireTtc']:
//           //   (this.origin === 'client') ? ligne.prixVenteUnitaireTtc : ligne.prixUnitaireTtc,
//           idEntreprise: idEnt
//         };
//       });
//
//       return {
//         id: this.idCommandeClient,
//         [this.origin]: this.selectedClientFournisseur, // clé dynamique 'client' ou 'fournisseur'
//         // id: (this.origin === 'client') ? this.idCommandeClient : this.idCommandeFournisseur,
//         code: this.codeCommandeClient,
//         // code: (this.origin === 'client') ?this.commandeClientDto.code : this.commandeFournisseurDto.code,
//         dateCommande: new Date().getTime(),
//         etatCommande: 'EN_PREPARATION',
//         idEntreprise: idEnt,
//         ['ligneCommandeClients']: lignesPourBackend
//         // [this.origin === 'client' ? 'ligneCommandeClients' : 'ligneCommandeFournisseurs']: lignesPourBackend
//       };
//     }else{
//       // On crée une copie des lignes pour ne pas modifier l'affichage en cours
//       const lignesPourBackend = this.listeLignesCommande.map(ligne => {
//         return {
//           // Très important : garder l'ID de la ligne si elle existe déjà
//           id: ligne.id || null,
//           article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
//           quantite: ligne.quantite,
//           prixUnitaireTtc: ligne.prixUnitaireTtc,
//           idEntreprise: idEnt
//         };
//       });
//
//       return {
//         id: this.idCommandeFournisseur,
//         [this.origin]: this.selectedClientFournisseur, // clé dynamique 'client' ou 'fournisseur'
//         code: this.codeCommandeFournisseur,
//         dateCommande: new Date().getTime(),
//         // dateCommande: new Date().getTime(),
//         etatCommande: 'EN_PREPARATION',
//         idEntreprise: idEnt,
//         ['ligneCommandeFournisseurs']: lignesPourBackend
//       };
//     }
//   }
//
//
//   cancelClick(): void {
//     this.router.navigate([this.origin === 'client' ?
//       'commandesclient' :
//       'commandesfournisseur'
//     ]);
//   }
//
//   private handleError(error: any): void {
//     this.errorMsg = error.error?.errors || [error.error?.message || 'Erreur'];
//   }
// }








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

