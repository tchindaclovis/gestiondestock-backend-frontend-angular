import {Component, Input, OnInit} from '@angular/core';
import {ArticleDto, CategoryDto, ClientDto, UtilisateurDto, VenteDto} from "../../../../gs-api/src";
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../../services/user/user.service";
import {ClientfournisseurService} from "../../../services/clientfournisseurs/clientfournisseur.service";
import {ArticleService} from "../../../services/article/article.service";
import {CategoryService} from "../../../services/category/category.service";
import {Observable} from "rxjs";
import {VenteService} from "../../../services/vente/vente.service";

@Component({
  selector: 'app-nouvelle-vente',
  templateUrl: './nouvelle-vente.component.html',
  styleUrls: ['./nouvelle-vente.component.scss']
})
export class NouvelleVenteComponent implements OnInit {

  @Input() origin = '';
  connectedUser: UtilisateurDto | null = null;
  clientFournisseur: any = {}; //soit client soit fournisseur

  codeArticle = '';
  quantite = '';
  codeVente = ''; // Lié au champ "Code vente"
  commentaire = ''; // Lié au champ "COmmentaire vente"
  idVente: number | null = null;


  selectedClientFournisseur: any = {}; // Variable utilisée dans le HTML
  listClients: ClientDto[] = [];

  searchedArticle: ArticleDto = {};
  listArticle: Array<ArticleDto> = [];



  articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
  venteDto: VenteDto = {}; //objet ou variable initialisé à vide
  errorMsg : Array<string> = [];
  // listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau

  listeLignesVente: Array<any> = [];
  totalVente = 0;
  articleNotYetSelected = false;

  constructor(
    private router: Router,
    private userService: UserService,
    private activatedRoute: ActivatedRoute,
    private clientFournisseurService: ClientfournisseurService,
    private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
    private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
    private venteService: VenteService
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();

    // 1. Déterminer l'origine
    this.activatedRoute.data.subscribe(data => {
      this.origin = 'client';

      // Charger la liste correspondante dès qu'on connaît l'origine
      this.findAllClientsFournisseurs();

      // 2. Charger les articles pour l'autocomplétion
      this.findAllArticles();

      // 3. Mode Modification / Visualisation
      // Récupération de l'ID depuis l'URL (ex: /nouvellevente/253)
      const id = this.activatedRoute.snapshot.params['idVente'];
      if (id) {
        this.idVente = id;
        this.chargerDonneesPourModification(id);
        this.venteService.findVenteById(id)
          .subscribe(vente =>{
            this.venteDto = vente;
          });
      }else{
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
    const serviceCall = this.venteService.findVenteById(id);

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
        const vte = JSON.parse(text);
        this.affecterDonnees(vte);
        this.chargerLignesVente(id);
      });
    }
    // Cas où la réponse est déjà un objet JSON
    else {
      this.affecterDonnees(res);
      this.chargerLignesVente(id);
    }
  }

  private affecterDonnees(vte: any): void {
    this.codeVente = vte.code || '';
    this.commentaire = vte.commentaire || '';
    // On récupère le client
    this.selectedClientFournisseur =  vte.client;
    console.log("Données affectées au formulaire :", this.codeVente);
  }


  private chargerLignesVente(idVente: number): void {
    // 1. Déclaration avec initialisation pour éviter l'erreur "used before being assigned"
    let serviceLignes: Observable<any>;
      serviceLignes = this.venteService.findAllLigneVenteByVentes(idVente);


    // 2. Appel du subscribe
    serviceLignes.subscribe({
      next: (res) => {
        if (res instanceof Blob) {
          // Utilisation de async/await ou .then pour extraire le texte du Blob
          res.text().then(text => {
            try {
              this.listeLignesVente = JSON.parse(text);
              this.calculerTotalVente();
            } catch (e) {
              console.error('Erreur de parsing JSON du Blob', e);
            }
          });
        } else {
          this.listeLignesVente = res;
          this.calculerTotalVente();
        }
        console.log("Lignes de vente :", this.listeLignesVente);
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des lignes', err);
      }
    });
  }


  calculerTotalVente(): void {
    this.totalVente = 0;
    this.listeLignesVente.forEach(lig => {
      // On récupère le prix disponible
      const prix = lig.prixVenteUnitaireTtc;
      this.totalVente += (+prix * +lig.quantite);
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

  ajouterLigneVente(): void {
    this.checkLigneVente();
    this.calculerTotalVente();
    // Reset
    this.searchedArticle = {};
    this.quantite = '';
    this.codeArticle = '';
    this.articleNotYetSelected = false;
  }


  private checkLigneVente(): void {
    const ligneExistante = this.listeLignesVente.find(lig =>
      lig.article?.codeArticle === this.searchedArticle.codeArticle
    );

    if (ligneExistante) {
      ligneExistante.quantite += +this.quantite;
    } else {

        const ligneVte = {
          article: this.searchedArticle,
          prixVenteUnitaireTtc: this.searchedArticle.prixVenteUnitaireTtc,
          quantite: +this.quantite
        };
        this.listeLignesVente.push(ligneVte);
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


  // private preparerVente(): any {
  //   return {
  //     id: this.idVente,
  //     // On s'assure d'envoyer l'objet client au bon champ
  //     client: this.selectedClientFournisseur,
  //     code: this.codeVente,
  //     commentaire: this.commentaire,
  //     dateVente: new Date().toISOString(), // Utiliser ISO String pour Jackson/Java Instant
  //     idEntreprise: this.connectedUser?.entreprise?.id,
  //     // LE CHAMP DOIT S'APPELER EXACTEMENT ligneVentes
  //     ligneVentes: this.listeLignesVente.map(ligne => ({
  //       id: ligne.id || null,
  //       article: ligne.article,
  //       quantite: ligne.quantite,
  //       prixVenteUnitaireTtc: ligne.prixVenteUnitaireTtc,
  //       idEntreprise: this.connectedUser?.entreprise?.id
  //     }))
  //   };
  // }


  private preparerVente(): any {
    const idEnt = this.connectedUser?.entreprise?.id;
    const lignesPourBackend = this.listeLignesVente.map(ligne => {
      return {
        // Très important : garder l'ID de la ligne si elle existe déjà
        id: ligne.id || null,
        article: { id: ligne.article?.id }, // Envoyer seulement l'ID article pour éviter les conflits
        // article: ligne.article,
        quantite: ligne.quantite,
        prixVenteUnitaireTtc: ligne.prixVenteUnitaireTtc,
        idEntreprise: idEnt
      };
    });

    return {
      // Si idVente existe, il est ajouté. Sinon, le backend créera une nouvelle entrée.
      id: this.idVente,
      [this.origin]: this.selectedClientFournisseur,
      code: this.codeVente,
      commentaire: this.commentaire,
      dateVente: new Date().getTime(),
      idEntreprise: idEnt,
      ['ligneVentes']: lignesPourBackend
    };
  }


  cancelClick(): void {
    this.router.navigate(['ventes']);
  }

  private handleError(error: any): void {
    this.errorMsg = error.error?.errors || [error.error?.message || 'Erreur'];
  }

}
