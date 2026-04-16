import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ArticleService} from "../../../services/article/article.service";
import {ArticleDto, CategoryDto, PhotoService, UtilisateurDto} from "../../../../gs-api/src";
import {CategoryService} from "../../../services/category/category.service";
declare var bootstrap: any;
@Component({
  selector: 'app-nouvel-article',
  templateUrl: './nouvel-article.component.html',
  styleUrls: ['./nouvel-article.component.scss']
})
export class NouvelArticleComponent implements OnInit {

  articleDto: ArticleDto = {}; //objet ou variable initialisé à vide
  categorieDto: CategoryDto = {};
  listeCategorie: Array<CategoryDto> = []; //liste de catégorie type tableau
  errorMsg: Array<string> = [];   //ou alors errorMsg: string[] = [];
  file: File| null = null;  // objet file qui peut être null et qui va être initialisé à null
  imgUrl: string | ArrayBuffer = 'assets/new_product.png';


  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute, //va permettre de récupérer l'id de l'article qu'on va passer en paramètre
    private articleService: ArticleService,   //injection du nouveau service article créé dans Angular
    private categoryService: CategoryService,  //injection du nouveau service category créé dans Angular
    private photoService: PhotoService,
  ) { }

  ngOnInit(): void {
    this.categoryService.findAll()
      .subscribe(categories => {
        this.listeCategorie = categories;
      });
    const idArticle = this.activatedRoute.snapshot.params['idArticle'];
    if(idArticle){
      this.articleService.findArticleById(idArticle)
      .subscribe(article =>{
        this.articleDto = article;
        this.categorieDto = this.articleDto.category ? this.articleDto.category : {};

        // CORRECTION : Si l'article a une photo (URL MinIO), on l'assigne à imgUrl
        if (this.articleDto.photo && this.articleDto.photo.startsWith('http')) {
          this.imgUrl = this.articleDto.photo;
        }
      });

    }else {

      this.articleService.getLastCodeArticle().subscribe({
        next: async (res: any) => { // Ajoutez 'async' ici
          let rawValue = res;

          // Si la réponse est un Blob, on extrait son contenu textuel
          if (res instanceof Blob) {
            rawValue = await res.text();
          }

          console.log('Valeur textuelle extraite :', rawValue); // Devrait afficher "ART0013"
          this.articleDto.codeArticle = this.genererProchainCode(rawValue);
        },
        error: (err) => {
          console.error('Erreur API :', err);
          this.articleDto.codeArticle = 'ART0001';
        }
      });


      // // MODE CRÉATION : On génère automatiquement le prochain code
      // this.articleService.getLastCodeArticle().subscribe({
      //   next: (lastCode) => {
      //     // lastCode sera "ART0000" (si vide) ou le dernier en BDD (ex: "ART0012")
      //     this.articleDto.codeArticle = this.genererProchainCode(lastCode);
      //   },
      //   error: (err) => {
      //     console.error('Erreur récupération dernier code', err);
      //     this.articleDto.codeArticle = 'ARTerror'; // Valeur de secours
      //   }
      // });
    }
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

    return `ART${formattedNumber}`;
  }

  // private genererProchainCode(lastCode: any): string {
  //   // 1. On force la conversion en string et on nettoie les éventuels guillemets
  //   let codeStr = String(lastCode).replace(/"/g, '');
  //
  //   // 2. Extraction de la partie numérique
  //   const numericPart = codeStr.replace('ART', '');
  //
  //   // 3. Conversion et incrémentation
  //   const nextNumber = parseInt(numericPart, 10) + 1;
  //
  //   // 4. Formatage
  //   return `ART${nextNumber.toString().padStart(4, '0')}`;
  // }



  cancelClick(): void{
    this.router.navigate(['articles']);
  }


  // verifierCodeArticle(): void {
  //   let code = this.articleDto.codeArticle;
  //   if (code) {
  //     this.articleService.findArticleByCode(code)
  //       .subscribe({
  //         next: (art) => {
  //           if (art && art.id) {
  //             alert("Code article numéro: " + code + " existe déjà dans la BDD, définissez-en un autre");
  //             this.articleDto.codeArticle = ''; // Optionnel : vide le champ
  //           }
  //         },
  //         error: () => {
  //           // Si erreur 404, c'est parfait, le code est libre
  //         }
  //       });
  //   }
  // }


  enregistrerArticle(): void {
    this.articleDto.category = this.categorieDto;
    this.articleService.enregistrerArticle(this.articleDto)
      .subscribe(art =>{
        this.savePhoto(art.id, art.codeArticle)
      }, error =>{
        this.errorMsg = error.error.errors;
      });
  }


  calculerTTC(): void {
    if (this.articleDto.prixVenteUnitaireHt && this.articleDto.tauxTva){
      this.articleDto.prixVenteUnitaireTtc =
        +this.articleDto.prixVenteUnitaireHt + (+(this.articleDto.prixVenteUnitaireHt*this.articleDto.tauxTva))/100
    }

    if (this.articleDto.prixUnitaireHt && this.articleDto.tauxTva){
      this.articleDto.prixUnitaireTtc =
        +this.articleDto.prixUnitaireHt + (+(this.articleDto.prixUnitaireHt*this.articleDto.tauxTva))/100
    }
  }


  onFileInput(files: FileList | null): void {
    if(files) {
      this.file = files.item(0);  //pour récupérer le premier fichier à l'index 0
      if (this.file){
        const fileReader = new FileReader();
        fileReader.readAsDataURL(this.file)  //pour afficher le fichier avant de l'enregistrer
        fileReader.onload = (event) => {
          if(fileReader.result){
            // Mise à jour de la prévisualisation avec le nouveau fichier sélectionné
            this.imgUrl = fileReader.result; //je peux changer ou mettre à jour le fichier
          }
        };
      }
    }
  }

  savePhoto(idArticle?: number, titre?: string): void {
    if (idArticle && titre && this.file) {  //si j'ai mon idArticle et un fichier sélectionné

      this.photoService.savePhoto(
        'article',        // context
        idArticle,        // id
        titre,            // title
        this.file         // file (Blob)
      ).subscribe({
        next: () => {
          this.router.navigate(['articles']);
        },
        error: (err) => {
          console.error('Erreur upload photo', err);
        }
      });
    } else {
      this.router.navigate(['articles']);
    }
  }


  extractMaxArticleCode(listeCodes: string[]): string | null {
    if (!listeCodes || listeCodes.length === 0) {
      return null;
    }

    // 1. Filtrer pour ne garder que les codes au bon format (ARTxxxx)
    // 2. Extraire la partie numérique et la convertir en nombre
    const numeros = listeCodes
      .filter(code => /^ART\d+$/.test(code)) // Vérifie que c'est ART suivi de chiffres
      .map(code => {
        const partieNumerique = code.replace('ART', '');
        return parseInt(partieNumerique, 10);
      });

    if (numeros.length === 0) return null;

    // 3. Trouver le nombre maximum
    const maxNumber = Math.max(...numeros);

    // 4. Reformater le nombre en "ARTxxxx" avec les zéros devant (ex: 5 -> ART0005)
    // On utilise padStart(4, '0') pour garder 4 chiffres après "ART"
    return `ART${maxNumber.toString().padStart(4, '0')}`;
  }


  autoResize(event: any) {
    event.target.style.height = 'auto';
    event.target.style.height = event.target.scrollHeight + 'px';
  }
}

