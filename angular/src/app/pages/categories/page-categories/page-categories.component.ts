import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
// @ts-ignore
import {CategoryDto, UtilisateurDto} from "../../../../gs-api/src";
import {CategoryService} from "../../../services/category/category.service";
import {ArticleService} from "../../../services/article/article.service";
import {UserService} from "../../../services/user/user.service";

@Component({
  selector: 'app-page-categories',
  templateUrl: './page-categories.component.html',
  styleUrls: ['./page-categories.component.scss']
})
export class PageCategoriesComponent implements OnInit {

  connectedUser: UtilisateurDto | null = null;
  listCategories: Array<CategoryDto> = [];
  selectedCatToDelete?: number = -1;  //initialisé à -1
  errorMsgs: '' | undefined;
  nombreArticle: number= 0;
  // Utilisation d'une Map pour stocker le nombre d'articles par ID de catégorie
  mapNombreArticles = new Map<number, number>();

  showModalError = false;
  messageErreur = '';

  constructor(
    private router: Router,
    private userService: UserService,
    private categoryService: CategoryService,
    private articleService: ArticleService
  ) { }

  ngOnInit(): void {
    // 1. IL FAUT RÉCUPÉRER L'UTILISATEUR CONNECTÉ ICI
    this.connectedUser = this.userService.getConnectedUser();
    this.findAllCategories();
  }

  findAllCategories(): void {
    this.categoryService.findAll()
      .subscribe(res => {
        this.listCategories = res;
        // Une fois les catégories chargées, on calcule le nombre d'articles pour chacune
        this.calculerNombreArticles();
      });
  }

  calculerNombreArticles(): void {
    this.listCategories.forEach(cat => {
      if (cat.id) {
        this.articleService.findAllArticleByIdCategory(cat.id)
          .subscribe(articles => {
            this.mapNombreArticles.set(cat.id!, articles.length);
          });
      }
    });
  }


  getNombreArticles(idCat?: number): number {
    if (idCat && this.mapNombreArticles.has(idCat)) {
      return this.mapNombreArticles.get(idCat)!;
    }
    return 0;
  }

  nouvelleCategory(): void{
    this.router.navigate(['nouvellecategorie'])
  }

  modifierCategory(id: number): void {

    // 1. Trouver la catégorie à modifier dans la liste locale
    const categorieAModifier = this.listCategories.find(cat => cat.id === id);

    if (!categorieAModifier) {
      this.messageErreur = "Catégorie introuvable.";
      this.showModalError = true;
      return;
    }

    // 2. Récupérer l'ID de l'utilisateur connecté et l'ID du créateur
    const idUtilisateurConnecte = this.connectedUser?.id;

    //2. Récupérer l'ID du créateur de la catégorie
    // 💡 Correction : On pointe vers le champ de traçabilité de l'utilisateur, pas vers l'ID de la catégorie
    const idCreateurCategorie = categorieAModifier?.idUtilisateur;  // Champ rempli par le backend

    // 3. Vérifier si l'utilisateur connecté est le créateur
    if (idUtilisateurConnecte && idUtilisateurConnecte === idCreateurCategorie) {
      this.router.navigate(['nouvellecategorie', id]);
    } else {
      // Si ce n'est pas le créateur, on affiche le modal
      this.messageErreur = "Seul l'utilisateur ayant créé cette catégorie a le droit de la modifier.";
      this.showModalError = true;
    }
  }

  confirmerEtSupprimerCat(): void {
    if(this.selectedCatToDelete !== -1){
      this.categoryService.delete(this.selectedCatToDelete)
        .subscribe(res =>{  //subscribe parle d'une action par l'opérateur (suppression)
          this.findAllCategories();
        }, error => {
          this.errorMsgs = error.error.message;
        });
    }
  }

  annulerSuppressionCat(): void {
    //En réaffectant la valeur -1, vous indiquez à Angular qu'aucune catégorie n'est plus sélectionnée
    // pour la suppression (car un ID en base de données commence toujours à 1, un ID de -1 est donc neutre et invalide).
    this.selectedCatToDelete = -1;
  }


  selectCatPourSupprimer(id?: number): void {
    if (!id) {
      this.messageErreur = "Catégorie introuvable.";
      this.showModalError = true;
      return;
    }
    // 1. Trouver la catégorie dans la liste locale pour analyser ses propriétés
    const catA_Supprimer = this.listCategories.find(cat => cat.id === id);

    // 2. RÈGLE DE GESTION : Vérifier si la catégorie possède des articles
    const nbreArticles = this.mapNombreArticles.get(id) || 0;
    const idUtilisateurConnecte = this.connectedUser?.id;
    const idCreateurCategorie = catA_Supprimer?.idUtilisateur; // ID du créateur configuré par le backend
    if (nbreArticles > 0) {
      this.messageErreur = "Cette catégorie est déjà associée à des articles.";
      this.showModalError = true;
      return; // On arrête l'exécution ici, le modal de confirmation Bootstrap ne doit pas s'ouvrir pour lui
    }else {
      if (idUtilisateurConnecte && idUtilisateurConnecte === idCreateurCategorie) {
        // 3. Si toutes les conditions sont validées, on mémorise l'ID pour la suppression
        this.selectedCatToDelete = id;
        return;
      }else{
        // 4. RÈGLE DE SÉCURITÉ : Seul le créateur peut supprimer
        this.messageErreur = "Seul l'utilisateur ayant créé cette catégorie a le droit de la supprimer.";
        this.showModalError = true;
        return; // On arrête l'exécution ici, le modal de confirmation Bootstrap ne doit pas s'ouvrir pour lui
      }
    }

    // // 2. RÈGLE DE GESTION : Vérifier si la catégorie possède des articles
    // const nbreArticles = this.mapNombreArticles.get(id) || 0;
    // if (nbreArticles > 0) {
    //   this.messageErreur = "Impossible de supprimer : cette catégorie est déjà associée à des articles.";
    //   this.showModalError = true;
    //   return;
    // }
    //
    // // 3. RÈGLE DE SÉCURITÉ : Seul le créateur peut supprimer
    // const idUtilisateurConnecte = this.connectedUser?.id;
    // const idCreateurCategorie = catA_Supprimer?.idUtilisateur; // ID du créateur configuré par le backend
    //
    // if (!idUtilisateurConnecte || idUtilisateurConnecte !== idCreateurCategorie) {
    //   this.messageErreur = "Seul l'utilisateur ayant créé cette catégorie a le droit de la supprimer.";
    //   this.showModalError = true;
    //   return; // On arrête l'exécution ici, le modal de confirmation Bootstrap ne doit pas s'ouvrir pour lui
    // }
  }

  // selectCatPourSupprimer(id?: number): void {    //point d'interrogation parceque l'Id peut être nul
  //   this.selectedCatToDelete = id;
  // }

}
