import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ArticleService} from "../../../services/article/article.service";
import {ArticleDto, CategoryDto, PhotoService} from "../../../../gs-api/src";
import {CategoryService} from "../../../services/category/category.service";

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
  imgUrl: string | ArrayBuffer = 'assets/product.png';

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
    }
  }


  cancelClick(): void{
    this.router.navigate(['articles']);
  }


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


  /**
   * Retourne l'URL de la photo.
   * Si la photo commence par 'http', on l'utilise directement.
   * Sinon, on affiche l'image par défaut.
   */
// Gardez cette méthode pour la sécurité, mais nous allons simplifier le HTML
  getPhotoUrl(): string | ArrayBuffer {
    return this.imgUrl;
  }

  // getPhotoUrl(photoName: string | undefined): string {
  //   if (!photoName) {
  //     // Chemin vers votre image par défaut dans le dossier assets d'Angular
  //     return 'assets/product.png';
  //   }
  //   // Utilisation du port API (9000) et du bucket 'imagephoto'
  //   return `http://localhost:9000/imagephoto/${photoName}`;
  // }

  autoResize(event: any) {
    event.target.style.height = 'auto';
    event.target.style.height = event.target.scrollHeight + 'px';
  }
}

