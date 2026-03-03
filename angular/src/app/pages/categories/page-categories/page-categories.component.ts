import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {CategoryDto} from "../../../../gs-api/src";
import {CategoryService} from "../../../services/category/category.service";

@Component({
  selector: 'app-page-categories',
  templateUrl: './page-categories.component.html',
  styleUrls: ['./page-categories.component.scss']
})
export class PageCategoriesComponent implements OnInit {

  listCategories: Array<CategoryDto> = [];
  selectedCatToDelete?: number = -1;  //initialisé à -1
  errorMsgs: '' | undefined;

  constructor(
    private router: Router,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    this.findAllCategories();
  }

  findAllCategories(): void{
    this.categoryService.findAll()
      .subscribe(res => {
        this.listCategories = res;
      });
  }

  nouvelleCategory(): void{
    this.router.navigate(['nouvellecategorie'])
  }

  modifierCategory(id: number): void {
    this.router.navigate(['nouvellecategorie', id])
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
    this.selectedCatToDelete = -1;
  }

  selectCatPourSupprimer(id?: number): void {    //point d'interrogation parceque l'Id peut être nul
    this.selectedCatToDelete = id;
  }
}
