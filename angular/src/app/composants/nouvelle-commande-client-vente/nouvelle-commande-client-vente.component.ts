import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {
  AdresseDto,
  ArticleDto,
  ClientDto,
  CommandeClientDto,
  FournisseurDto,
  PhotoService,
  UtilisateurDto,
  VenteDto
} from "../../../gs-api/src";
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../services/user/user.service";
import {ClientfournisseurService} from "../../services/clientfournisseurs/clientfournisseur.service";
import {ArticleService} from "../../services/article/article.service";
import {CategoryService} from "../../services/category/category.service";
import {VenteService} from "../../services/vente/vente.service";
import {Observable} from "rxjs";
import {
  CommandeclientfournisseurService
} from "../../services/commandeclientfournisseur/commandeclientfournisseur.service";

@Component({
  selector: 'app-nouvelle-commande-client-vente',
  templateUrl: './nouvelle-commande-client-vente.component.html',
  styleUrls: ['./nouvelle-commande-client-vente.component.scss']
})
export class NouvelleCommandeClientVenteComponent implements OnInit {

  @Input() origin = '';
  // @Input() commande: any = {};
  connectedUser: UtilisateurDto | null = null;
  clientFournisseur: any = {}; //soit client soit fournisseur

  codeArticle = '';
  quantite = '';
  codeVente = ''; // Lié au champ "Code vente client"
  codeCommandeClient = ''; // Lié au champ "Code vente client"
  codeCommandeFournisseur = ''; // Lié au champ "Code commande fournisseur"
  idCommandeClient: number | null = null;
  idVente: number | null = null;

  selectedClientFournisseur: any = {}; // Variable utilisée dans le HTML
  listClients: ClientDto[] = [];
  listFournisseurs: FournisseurDto[] = [];

  searchedArticle: ArticleDto = {};
  listArticle: Array<ArticleDto> = [];

  commandeClientDto: CommandeClientDto = {}; //objet ou variable initialisé à vide
  venteDto: VenteDto = {}; //objet ou variable initialisé à vide
  adresseDto: AdresseDto = {};

  articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
  errorMsg : Array<string> = [];
  // listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau

  listeLignesCommande: Array<any> = [];
  listeLignesVente: Array<any> = [];
  totalCommande = 0;
  articleNotYetSelected = false;

  // On récupère les valeurs de l'énumération pour l'itérer dans le HTML
  paymentTypesOptions = Object.values(VenteDto.PaymentTypeEnum);

  file: File| null = null;  // objet file qui peut être null et qui va être initialisé à null
  imgUrl: string | ArrayBuffer = 'assets/new_product.png';

  constructor(
    private router: Router,
    private userService: UserService,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
    private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
    private venteService: VenteService,
    private commandeClientFournisseurService: CommandeclientfournisseurService,
    private photoService: PhotoService
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();

    // 1. Déterminer l'origine
    this.activatedRoute.data.subscribe(data => {
      this.origin = 'client';
      // this.extractClientFournisseur();

      // Charger la liste correspondante dès qu'on connaît l'origine
      this.findAllClientsFournisseurs();

      // 2. Charger les articles pour l'autocomplétion
      this.findAllArticles();

      // 3. Mode Modification / Visualisation
      // Récupération de l'ID depuis l'URL (ex: /nouvellevente/253)
      const id = this.activatedRoute.snapshot.params['idCommandeClient'];
      if (id) {
        this.idCommandeClient = id;
        this.chargerDonneesPourModification(id);
        this.commandeClientFournisseurService.findCommandeClientById(id)
          .subscribe(commande =>{
            this.commandeClientDto = commande;
          });

      // }else{  //on suspend la condition sur l'ID si la commande est modifiée uniquement pour créer une vente

        this.venteService.getLastCodeVente().subscribe({
          next: async (res: any) => { // Ajoutez 'async' ici
            let rawValue = res;
            // Si la réponse est un Blob, on extrait son contenu textuel
            if (res instanceof Blob) {
              rawValue = await res.text();
            }
            console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "ART0013"
            this.codeVente = this.genererProchainCode(rawValue);
          },
          error: (err) => {
            console.error('Erreur API :', err);
            this.codeVente = 'CVT0001';
          }
        });
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

    // 4. Formatage : "ART" + nombre formaté sur 4 positions (Milliers, Centaines, Dizaines, Unités)
    // padStart(4, '0') transforme 14 en "0014"
    const formattedNumber = nextNumber.toString().padStart(4, '0');

    return `CVT${formattedNumber}`;
  }


  chargerDonneesPourModification(id: number): void {
    console.log("ID détecté :", id, " | Origine :", this.origin);

    // On définit l'appel dynamiquement
    const serviceCall = this.commandeClientFournisseurService.findCommandeClientById(id) ;

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
    this.idCommandeClient = cmd.id; // On stocke l'ID reçu
    // this.codeCommandeClient = cmd.code || ''; //la commande est engagée pour la vente
    this.commandeClientDto = cmd.etatCommande || '';
    this.clientFournisseur = cmd.client || {}; // On récupère soit le client selon l'origine
    this.adresseDto = (cmd.client && cmd.client.adresse) ? cmd.client.adresse : {};
    // console.log("Données affectées au formulaire :", this.codeCommandeClient);
  }

  private chargerLignesCommande(idCmd: number): void {
    // 1. Déclaration avec initialisation pour éviter l'erreur "used before being assigned"
    let serviceLignes: Observable<any>;
    serviceLignes = this.commandeClientFournisseurService.findAllLigneCommandesClientByCommande(idCmd);

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
        console.log("Lignes de commande :", this.listeLignesCommande);
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
      const prix = lig.prixVenteUnitaireTtc;
      this.totalCommande += (+prix * +lig.quantite);
    });
  }

  // ==============================
  // LOGIQUE METIER (Correction des liaisons)
  // ==============================
  findAllClientsFournisseurs(): void {
    const idEntreprise = this.connectedUser?.entreprise?.id;
    if (idEntreprise) {
      this.clientFournisseurService.findAllClientByIdEntreprise(idEntreprise)
        .subscribe(res => this.listClients = res);
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


  enregistrerVente(): void {
    const vente = this.preparerVente();
    this.venteService.enregistrerVente(vente).subscribe({
      next: () => {
        // Redirection vers la liste après succès
        this.router.navigate(['ventes']);
      },
      error: (e) => this.handleError(e)
    });
  }


  private preparerVente(): any {
    // let codeCmdClient = this.commandeClientDto?.code
    // const idEnt = this.connectedUser?.entreprise?.id;
    const lignesPourBackend = this.listeLignesCommande.map(ligne => {
      return {
        // Très important : garder l'ID de la ligne si elle existe déjà
        id: ligne.id || null,
        article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
        // article: ligne.article,
        quantite: ligne.quantite,
        prixVenteUnitaireTtc: ligne.prixVenteUnitaireTtc,
        idEntreprise: this.connectedUser?.entreprise?.id
      };
    });

    return {
      // Si idVente existe, il est ajouté. Sinon, le backend créera une nouvelle entrée.
      id: this.idVente,
      [this.origin]: this.clientFournisseur,
      code: this.codeVente,
      codeCommandeClient: this.commandeClientDto?.code,
      paymentType: this.venteDto.paymentType,
      dateVente: new Date().getTime(),
      idEntreprise: this.connectedUser?.entreprise?.id,
      ['ligneVentes']: lignesPourBackend
    };
  }


  cancelClick(): void {
    this.router.navigate(['commandesclient']);
  }

  private handleError(error: any): void {
    this.errorMsg = error.error?.errors || [error.error?.message || 'Erreur'];
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

}




// enregistrerCommande(): void {
//   const commande = this.preparerCommande();
//   if (this.origin === 'client') {
//     this.commandeClientFournisseurService.enregistrerCommandeClient(commande).subscribe({
//       next:() =>{
//         this.router.navigate(['commandesclient']);
//       },
//       error: (e) => this.handleError(e)
//     });
//   } else if (this.origin === 'fournisseur') {
//     this.commandeClientFournisseurService.enregistrerCommandeFournisseur(commande).subscribe({
//       next: () => {
//         this.router.navigate(['commandesfournisseur'])
//       },
//       error: (e) => this.handleError(e)
//     });
//   }
// }



// private preparerCommande(): any {
//   const idEnt = this.connectedUser?.entreprise?.id;
//   // Utiliser l'ID général récupéré lors du ngOnInit (idCommande)
//   const currentId = this.idCommandeClient;
//
//   // On détermine l'état :
//   // Si c'est une nouvelle commande (id null), on met PRO_FORMAT.
//   // Si c'est une modif, on garde l'état actuel (qui pourrait être déjà CONFIRMEE).
//   const etatActuel =  this.commandeClientDto.etatCommande  ;// Attention ici, vérifiez bien vos noms de variables (votre code mélangeait Dto client/fournisseur)
//
//
//   const lignesPourBackend = this.listeLignesCommande.map(ligne => {
//     return {
//       id: ligne.id || null, // CRITIQUE : Garder l'ID de la ligne existante
//       article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
//       quantite: ligne.quantite,
//       // On affecte le bon prix selon le contexte
//       prixVenteUnitaireTtc: (this.origin === 'client') ? ligne.prixVenteUnitaireTtc : undefined,
//       prixUnitaireTtc: (this.origin === 'fournisseur') ? ligne.prixUnitaireTtc : undefined,
//       idEntreprise: idEnt
//     };
//   });
//
//   return {
//     id: currentId, // Si présent, Hibernate fera un UPDATE // CRITIQUE : L'ID de la commande pour déclencher l'UPDATE au lieu du INSERT
//     [this.origin]: this.clientFournisseur,
//     // client: { id: this.selectedClientFournisseur?.id },
//     code: (this.origin === 'client') ? this.codeCommandeClient : this.codeCommandeFournisseur,
//     dateCommande: new Date().toISOString(), // Utiliser ISOString pour la stabilité
//     etatCommande: currentId ? (etatActuel || 'PRO_FORMAT') : 'PRO_FORMAT',
//     idEntreprise: idEnt,
//     [this.origin === 'client' ? 'ligneCommandeClients' : 'ligneCommandeFournisseurs']: lignesPourBackend
//   };
// }
