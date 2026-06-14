import {Component, Input, OnInit} from '@angular/core';
import {
  AdresseDto,
  ArticleDto,
  ClientDto,
  CommandeClientDto,
  PhotoService,
  UtilisateurDto,
  VenteDto
} from "../../../../gs-api/src";
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../../services/user/user.service";
import {ClientfournisseurService} from "../../../services/clientfournisseurs/clientfournisseur.service";
import {ArticleService} from "../../../services/article/article.service";
import {CategoryService} from "../../../services/category/category.service";
import {Observable} from "rxjs";
import {VenteService} from "../../../services/vente/vente.service";
import {
  CommandeclientfournisseurService
} from "../../../services/commandeclientfournisseur/commandeclientfournisseur.service";

@Component({
  selector: 'app-nouvelle-vente',
  templateUrl: './nouvelle-vente.component.html',
  styleUrls: ['./nouvelle-vente.component.scss']
})
export class NouvelleVenteComponent implements OnInit {

  @Input() origin = '';
  connectedUser: UtilisateurDto | null = null;
  clientFournisseur: any = {}; //soit client soit fournisseur
  adresseDto: AdresseDto = {};
  commandeClientDto: CommandeClientDto = {}; //objet ou variable initialisé à vid
  codeArticle = '';
  quantite = '';
  codeVente = ''; // Lié au champ "Code vente"
  codeCommandeClient= ''; // Lié au champ "Code commande client"
  paymentType = ''; // Lié au champ "PaymentType"
  // adresse1 = ''; // Lié au champ "adresse1 de  vente"
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
    private photoService: PhotoService,
    private commandeClientFournisseurService: CommandeclientfournisseurService // 👈 Ajoutez l'injection de votre service ici
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();

    // 1. Déterminer l'origine
    this.activatedRoute.data.subscribe(data => {
      this.origin = 'client';

      // Charger la liste correspondante dès qu'on connaît l'origine
      this.findAllClientsFournisseurs();

      // this.findClientFournisseur();

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
    this.idVente = vte.id;
    this.codeVente = vte.code || '';

    // Crucial : On met à jour les deux variables pour garantir que le lien n'est pas rompu
    this.codeCommandeClient = vte.codeCommandeClient || '';
    this.commandeClientDto = { code: vte.codeCommandeClient };

    this.venteDto = vte;
    this.paymentType = vte.paymentType || '';


    // 🛡️ RÉCUPÉRATION DE L'ÉTAT ET DES INFOS DE LA COMMANDE
    if (this.codeCommandeClient && this.codeCommandeClient.trim() !== '') {
      // On force le typage à 'any' temporairement pour la vérification du Blob ou du parsing
      this.commandeClientFournisseurService.findCommandeClientByCode(this.codeCommandeClient).subscribe({
        next: (commande: any) => {
          // 1. Correction Erreur 1 & 2 : Cast en 'any' pour éviter les erreurs d'instanceof et d'assignation brute
          if (commande instanceof Blob) {
            (commande as Blob).text().then(text => {
              this.commandeClientDto = JSON.parse(text);
            });
          } else if (typeof commande === 'string') {
            this.commandeClientDto = JSON.parse(commande);
          } else {
            this.commandeClientDto = commande;
          }
          console.log("Commande chargée pour affichage de l'état :", this.commandeClientDto);
        },
        error: (err) => {
          console.error("Impossible de récupérer les détails de la commande d'origine", err);
          // 2. Correction Erreur 3 : Utiliser l'énumération générée par Swagger/OpenAPI pour l'état par défaut
          this.commandeClientDto = {
            code: this.codeCommandeClient,
            // On évite d'écrire 'INCONNU' en dur et on utilise le bon type ou on le laisse indéfini
            etatCommande: undefined
          };
        }
      });
    } else {
      this.commandeClientDto = {}; // Pas de commande liée (Vente directe)
    }

    // // 🛡️ RÉCUPÉRATION DE L'ÉTAT ET DES INFOS DE LA COMMANDE
    // if (this.codeCommandeClient && this.codeCommandeClient.trim() !== '') {
    //   // On appelle le backend pour récupérer l'objet complet (avec état, date, etc.)
    //   this.commandeClientFournisseurService.findCommandeClientByCode(this.codeCommandeClient).subscribe({
    //     next: (commande) => {
    //       if (commande instanceof Blob) {
    //         (commande as Blob).text().then(text => {
    //           this.commandeClientDto = JSON.parse(text);
    //         });
    //       } else {
    //         this.commandeClientDto = commande;
    //       }
    //       console.log("Commande chargée pour affichage de l'état :", this.commandeClientDto);
    //     },
    //     error: (err) => {
    //       console.error("Impossible de récupérer les détails de la commande d'origine", err);
    //       // En cas d'erreur, on garde une structure minimale
    //       this.commandeClientDto = { code: this.codeCommandeClient, etatCommande: 'INCONNU' };
    //     }
    //   });
    // } else {
    //   this.commandeClientDto = {}; // Pas de commande liée (Vente directe)
    // }


    // On récupère le client et son adresse de manière sécurisée
    this.clientFournisseur = vte.client || {};
    if (vte.client && vte.client.adresse) {
      this.adresseDto = vte.client.adresse;
    } else {
      this.adresseDto = {};
    }

    this.imgUrl = vte.client?.photo || 'assets/new_product.png';
    console.log("Données affectées au formulaire. Code Commande :", this.codeCommandeClient);
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
    // 1. On s'assure d'associer la bonne adresse mise à jour au client avant la sauvegarde
    this.clientFournisseur.adresse = this.adresseDto;
    this.clientFournisseur.idEntreprise = this.connectedUser?.entreprise?.id;

    // 2. Étape 1 : Sauvegarder/Mettre à jour le client d'abord le client/fournisseur pour obtenir un ID valide
    this.clientFournisseurService.enregistrerClient(this.clientFournisseur).subscribe({
      next: (clientSauvegarde) => {
        console.log('Client sauvegardé avec succès, ID :', clientSauvegarde.id);

        // On met à jour notre objet local avec l'ID et les infos retournées par le serveur
        this.clientFournisseur = clientSauvegarde;

        // 3. Étape 2 : Enregistrer la vente principale (Préparer et sauvegarder la vente avec le client persistant)
        const vente = this.preparerVente();

        this.venteService.enregistrerVente(vente).subscribe({
          next: (venteSauvegardee) => {
            console.log('Vente enregistrée avec succès');

            // 4. Étape 3 : Gestion de la photo (Si une photo a été sélectionnée, on l'enregistre sur le client)
            if (this.file && clientSauvegarde.id) {
              const nomPhoto = clientSauvegarde.nom || 'photo_client';
              this.savePhoto(clientSauvegarde.id, nomPhoto);
            } else {
              // Pas de photo ? On redirige directement
              this.router.navigate(['ventes']);
            }
          },
          error: (e) => this.handleError(e)
        });
      },
      error: (e) => this.handleError(e)
    });
  }

  private preparerVente(): any {
// On lie l'adresse modifiée au client sans écraser les champs manquants par "NR"
    this.clientFournisseur.adresse = this.adresseDto;
    const lignesPourBackend = this.listeLignesVente.map(ligne => {
      return {
        id: ligne.id || null,  // Très important : garder l'ID de la ligne si elle existe déjà
        article: {
          id: ligne.article?.id  // Envoyer seulement l'ID article pour éviter les conflits
        },
        quantite: ligne.quantite,
        prixVenteUnitaireTtc: ligne.prixVenteUnitaireTtc,
        idEntreprise: this.connectedUser?.entreprise?.id
      };
    });

    return {
      id: this.idVente, // Si idVente existe, il est ajouté. Sinon, le backend créera une nouvelle entrée.
      [this.origin]: this.clientFournisseur, // On passe l'objet complet mis à jour localement
      // [this.origin]: {   // Ici, this.clientFournisseur contient désormais l'id généré par le backend
      //   id: this.clientFournisseur.id
      // },
      code: this.codeVente,
      codeCommandeClient: this.commandeClientDto?.code || this.codeCommandeClient, // On s'assure de récupérer le code de la commande depuis l'une des deux sources disponibles
      paymentType: this.venteDto.paymentType,
      dateVente: this.venteDto.dateVente || new Date().getTime(),
      idEntreprise: this.connectedUser?.entreprise?.id, // const idEnt = this.connectedUser?.entreprise?.id;
      ['ligneVentes']: lignesPourBackend
    };
  }


  cancelClick(): void {
    this.router.navigate(['ventes']);
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
