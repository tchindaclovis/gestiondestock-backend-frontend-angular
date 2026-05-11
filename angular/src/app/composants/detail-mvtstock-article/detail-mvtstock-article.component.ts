import {Component, Input, OnInit, Output, EventEmitter, SimpleChanges} from '@angular/core';
import {ArticleDto, VenteDto} from '../../../gs-api/src';
import { MvtstockService } from '../../services/mvtstock/mvtstock.service';
import {Router} from "@angular/router";
import { MvtStockDto } from "../../../gs-api/src/model/mvtStockDto";
import {ArticleService} from "../../services/article/article.service";


@Component({
  selector: 'app-detail-mvtstock-article',
  templateUrl: './detail-mvtstock-article.component.html',
  styleUrls: ['./detail-mvtstock-article.component.scss']
})
export class DetailMvtstockArticleComponent implements OnInit {

  @Input() articleDto: ArticleDto = {};
  @Input() stockGlobal: number = 0;


  // Objet local pour le formulaire
  mvtStockDto: MvtStockDto = {
    quantite: 0,
    typeMvt: 'CORRECTION_POS', // Valeur par défaut
    sourceMvt: 'VENTE'
  };
  codeCorrection = ''; // Lié au champ "Code correction"

  // @Output() suppressionResult = new EventEmitter<string>();

  // Émetteur pour demander au parent de rafraîchir la liste/stock après correction
  @Output() correctionStockEvent = new EventEmitter<string>();

  // Déclarez la variable sans l'initialiser avec Object.values ici
  sourceMvtsOptions: string[] = [];
  typeMvtsOptions: string[] = [];


  constructor(
    private router: Router,
    private mvtstockService: MvtstockService, // Injectez le service de mouvements
    private articleService:ArticleService

  ) { }

  // ==============================
  // INITIALISATION
  // ==============================
  ngOnInit(): void {
    // Initialisez la liste ici pour éviter les erreurs de compilation
    if (MvtStockDto.TypeMvtEnum) {
      this.typeMvtsOptions = Object.values(MvtStockDto.TypeMvtEnum);
    }
    if (MvtStockDto.SourceMvtEnum) {
      this.sourceMvtsOptions = Object.values(MvtStockDto.SourceMvtEnum);
    }
    this.chargerStock();


    // this.mvtstockService.getLastCodeCorrection().subscribe({
    //   next: async (res: any) => {
    //     let rawValue = '';
    //
    //     if (res instanceof Blob) {
    //       rawValue = await res.text();
    //     } else {
    //       rawValue = String(res);
    //     }
    //
    //     // Supprimer les guillemets résiduels si le backend renvoie ""CCS0001""
    //     rawValue = rawValue.replace(/"/g, '');
    //
    //     console.log('Dernier code reçu du serveur :', rawValue);
    //
    //     // Affectation du nouveau code incrémenté
    //     this.codeCorrection = this.genererProchainCode(rawValue);
    //   },
    //   error: (err) => {
    //     console.warn('Aucun code trouvé ou erreur, initialisation à CCS0001');
    //     this.codeCorrection = 'CCS0001';
    //   }
    // });

    this.mvtstockService.getLastCodeCorrection().subscribe({
      next: async (res: any) => { // Ajoutez 'async' ici
        let rawValue = res;
        // Si la réponse est un Blob, on extrait son contenu textuel
        if (res instanceof Blob) {
          rawValue = await res.text();
        }
        console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "ART0013"
        this.codeCorrection = this.genererProchainCode(rawValue);
      },
      error: (err) => {
        console.error('Erreur API :', err);
        this.codeCorrection = 'CCS0001';
      }
    });
  }

  // ==============================
  // DETECTION DES CHANGEMENTS INPUT
  // ==============================
// Indispensable pour mettre à jour le stock quand on change d'article dans la liste
  /**
   * Si l'article change (Input modifié)
   * → recalcul du stock
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['articleDto'] && this.articleDto?.id) {
      this.chargerStock();
    }
  }


  private genererProchainCode(lastCode: any): string {
    console.log('Type de lastCode :', typeof lastCode);
    console.log('Valeur brute de lastCode :', lastCode);
    // 1. Nettoyage : on transforme en string et on enlève TOUT ce qui n'est pas un chiffre
    // Cela gère les "CCS", les guillemets double "", les espaces, etc.
    const rawValue = String(lastCode);
    const onlyNumbers = rawValue.replace(/\D/g, ''); // \D signifie "tout ce qui n'est pas un chiffre"

    let nextNumber = 0;

    if (onlyNumbers.length > 0) {
      // 2. Si on a trouvé des chiffres (ex: "0000"), on ajoute 1
      nextNumber = parseInt(onlyNumbers, 10) + 1;
    } else {
      // 3. Si aucun chiffre n'est trouvé, on commence à 1
      nextNumber = 1;
    }

    // 4. On reformate avec le préfixe et 4 chiffres (ex: CCS0001)
    return `CCS${nextNumber.toString().padStart(4, '0')}`;
  }


  // private genererProchainCode(lastCode: any): string {
  //   console.log('Type de lastCode :', typeof lastCode);
  //   console.log('Valeur brute de lastCode :', lastCode);
  //   // 1. Si lastCode est vide, nul ou non défini, on commence à 1
  //   if (!lastCode || lastCode === '' || lastCode === 'null') {
  //     return 'CCS0001';
  //   }
  //
  //   // 2. Nettoyage de la chaîne
  //   const cleanCode = String(lastCode).replace(/["\s\n\r]/g, '');
  //
  //   // 3. Extraction des chiffres
  //   const match = cleanCode.match(/\d+/);
  //
  //   if (match) {
  //     // On extrait le nombre, on l'incrémente
  //     const nextNumber = parseInt(match[0], 10) + 1;
  //     // On formate avec des zéros non significatifs (ex: 0002)
  //     return `CCS${nextNumber.toString().padStart(4, '0')}`;
  //   }
  //
  //   // Si on a un texte sans chiffre, on renvoie le premier code
  //   return 'CCS0001';
  // }


  // private genererProchainCode(lastCode: any): string {
  //   console.log('Type de lastCode :', typeof lastCode);
  //   console.log('Valeur brute de lastCode :', lastCode);
  //   // 1. Conversion en string et nettoyage radical (supprime guillemets, espaces, retours à la ligne)
  //   const cleanCode = String(lastCode).replace(/["\s\n\r]/g, '');
  //
  //   // 2. Extraction de TOUS les chiffres présents dans la chaîne
  //   // On cherche une suite de chiffres (\d+)
  //   const match = cleanCode.match(/\d+/);
  //
  //   let nextNumber = 9999; // Valeur par défaut si aucun chiffre n'est trouvé
  //
  //   if (match && match[0]) {
  //     // 3. Conversion de la partie trouvée (ex: "0013") en nombre et incrémentation
  //     nextNumber = parseInt(match[0], 10) + 1;
  //   }
  //
  //   // 4. Formatage : "ART" + nombre formaté sur 4 positions (Milliers, Centaines, Dizaines, Unités)
  //   // padStart(4, '0') transforme 14 en "0014"
  //   const formattedNumber = nextNumber.toString().padStart(4, '0');
  //
  //   return `CCS${formattedNumber}`;
  // }


  private chargerStock(): void {
    if (this.articleDto && this.articleDto.id) {
      this.mvtstockService.stockReelArticle(this.articleDto.id)
        .subscribe({
          next: (res: any) => {
            // Cas 1 : La réponse est un Blob (votre erreur actuelle)
            if (res instanceof Blob) {
              const reader = new FileReader();
              reader.onload = () => {
                const value = reader.result as string;
                this.stockGlobal = Number(value) || 0;
              };
              reader.readAsText(res);
            }
            // Cas 2 : La réponse est déjà un nombre ou un texte
            else {
              this.stockGlobal = Number(res) || 0;
            }
          },
          error: (err) => {
            console.error("Erreur lors de la récupération du stock", err);
            this.stockGlobal = 0;
          }
        });
    }
  }


  enregistrerCorrection(): void {

    // 1. Validation de base
    if (!this.articleDto.id || !this.mvtStockDto.quantite || !this.mvtStockDto.typeMvt) {
      console.error("Données de correction incomplètes");
      return;
    }
    // 2. Préparation du DTO avec les attributs nécessaires
    this.mvtStockDto.article = this.articleDto;
    this.mvtStockDto.dateMvt = new Date().toISOString();
      // On récupère l'idEntreprise de l'article (car l'article appartient à l'entreprise)
      this.mvtStockDto.idEntreprise = this.articleDto.idEntreprise;

      // --- LA LIGNE À AJOUTER ---
      // On mappe le code de la modal vers le champ codeSource du DTO
      this.mvtStockDto.codeSource = this.codeCorrection;
      // --------------------------

      // 3. Détermination de la source automatique pour les corrections
      this.mvtStockDto.sourceMvt = 'CORRECTION_STOCK';

   // AJOUTE CETTE LIGNE :
    let call;
      // 4. Choix du service selon le type de mouvement
    switch (this.mvtStockDto.typeMvt) {
      case 'CORRECTION_POS':
        call = this.mvtstockService.correctionStockPos(this.mvtStockDto);
        break;
      case 'CORRECTION_NEG':
        call = this.mvtstockService.correctionStockNeg(this.mvtStockDto);
        break;
      case 'CORRECTION_POS_VENTE_RED':
        call = this.mvtstockService.correctionStockPosVenteRed(this.mvtStockDto);
        break;

      case 'CORRECTION_NEG_VENTE_AUG':
        call = this.mvtstockService.correctionStockNegVenteAug(this.mvtStockDto);
        break;

      case 'CORRECTION_NEG_RETOUR_FOURNISSEUR':
        call = this.mvtstockService.correctionStockNegRetourFournisseur1(this.mvtStockDto);
        break;

      default:
        // Optionnel : gérer un cas par défaut ou une erreur
        console.warn('Type de mouvement non reconnu');
        return; // On sort de la méthode si le type est inconnu
    }


    // 5.Exécution de l'appel API
    call.subscribe({
      next: () => {
        // 1. Réinitialiser le formulaire local
        this.handleSuccess();

        // 2. Recharger le stock global de cet article précisément
        this.chargerStock(); //Rafraîchit la vue locale de l'article

        // 3. Notifier le parent pour qu'il recharge la liste des mouvements
        // On passe l'ID de l'article pour que le parent sache qui mettre à jour
        this.correctionStockEvent.emit('success');
      },
      error: (err) => {
        console.error("Erreur lors de la correction", err)
      }
    });
  }



  private handleSuccess(): void {
    // On récupère le numéro actuel pour l'incrémenter manuellement pour la prochaine saisie
    const currentNum = parseInt(this.codeCorrection.replace('CCS', ''), 10);
    const nextNum = isNaN(currentNum) ? 1 : currentNum + 1;

    // Réinitialisation du formulaire
    this.mvtStockDto = {
      quantite: 0,
      typeMvt: 'CORRECTION_POS'
    };

    // On prépare déjà le code suivant pour la prochaine utilisation de la modal
    this.codeCorrection = `CCS${nextNum.toString().padStart(4, '0')}`;
    // this.codeCorrection = '';
  }

  enregistrerQuantiteAlert(): void {
    // On vérifie que l'article et son ID existent bien
    if (this.articleDto && this.articleDto.id) {
      this.articleService.enregistrerArticle(this.articleDto)
        .subscribe({
          next: (res) => {
            // Optionnel : Tu peux ajouter un petit message de succès ou un toast ici
            console.log('Quantité d\'alerte mise à jour avec succès', res);
          },
          error: (err) => {
            // Gestion de l'erreur (affichage dans la console ou via un service d'alerte)
            console.error('Erreur lors de la mise à jour de la quantité d\'alerte', err);
          }
        });
    }
  }
}



// enregistrerCorrection(): void {
//   if (!this.articleDto.id || !this.mvtStockDto.quantite) {
//     return;
//   }
//   // On prépare le DTO avec l'article concerné
//   this.mvtStockDto.article = this.articleDto;
//   this.mvtStockDto.dateMvt = new Date().toISOString();
//
//   if (this.mvtStockDto.typeMvt === 'CORRECTION_POS') {
//     this.mvtstockService.correctionStockPos(this.mvtStockDto).subscribe({
//       next: () => {
//         this.handleSuccess();
//         // 1. On émet d'abord le succès pour les éventuels composants parents
//         // On prévient le parent pour mettre à jour l'affichage du stock actuel
//         this.correctionStockEvent.emit('success');
//         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
//         setTimeout(() => {
//           // 3. Navigation vers la liste des ventes
//           // Cela déclenchera le rechargement du composant de la liste
//           this.router.navigate(['mvtstock']);
//         }, 300);
//       },
//       error: (err) => console.error(err)
//     });
//   } else  if (this.mvtStockDto.typeMvt === 'CORRECTION_NEG'){
//     this.mvtstockService.correctionStockNeg(this.mvtStockDto).subscribe({
//       next: () => {
//         this.handleSuccess();
//         // 1. On émet d'abord le succès pour les éventuels composants parents
//         // On prévient le parent pour mettre à jour l'affichage du stock actuel
//         this.correctionStockEvent.emit('success');
//         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
//         setTimeout(() => {
//           // 3. Navigation vers la liste des ventes
//           // Cela déclenchera le rechargement du composant de la liste
//           this.router.navigate(['mvtstock']);
//         }, 300);
//       },
//       error: (err) => console.error(err)
//     });
//   } else  if (this.mvtStockDto.typeMvt === 'CORRECTION_POS_VENTE_RED'){
//     this.mvtstockService.correctionStockPosVenteRed(this.mvtStockDto).subscribe({
//       next: () => {
//         this.handleSuccess();
//         // 1. On émet d'abord le succès pour les éventuels composants parents
//         // On prévient le parent pour mettre à jour l'affichage du stock actuel
//         this.correctionStockEvent.emit('success');
//         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
//         setTimeout(() => {
//           // 3. Navigation vers la liste des ventes
//           // Cela déclenchera le rechargement du composant de la liste
//           this.router.navigate(['mvtstock']);
//         }, 300);
//       },
//       error: (err) => console.error(err)
//     });
//   } else  if (this.mvtStockDto.typeMvt === 'CORRECTION_NEG_VENTE_AUG'){
//     this.mvtstockService.correctionStockNegVenteAug(this.mvtStockDto).subscribe({
//       next: () => {
//         this.handleSuccess();
//         // 1. On émet d'abord le succès pour les éventuels composants parents
//         // On prévient le parent pour mettre à jour l'affichage du stock actuel
//         this.correctionStockEvent.emit('success');
//         // 2. On attend la fermeture de la modal (300ms) avant de naviguer
//         setTimeout(() => {
//           // 3. Navigation vers la liste des ventes
//           // Cela déclenchera le rechargement du composant de la liste
//           this.router.navigate(['mvtstock']);
//         }, 300);
//       },
//       error: (err) => console.error(err)
//     });
//   }
// }



