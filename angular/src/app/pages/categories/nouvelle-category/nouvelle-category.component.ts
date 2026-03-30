import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {CategoryDto} from "../../../../gs-api/src";
import {CategoryService} from "../../../services/category/category.service";

@Component({
  selector: 'app-nouvelle-category',
  templateUrl: './nouvelle-category.component.html',
  styleUrls: ['./nouvelle-category.component.scss']
})
export class NouvelleCategoryComponent implements OnInit {

  categoryDto: CategoryDto = {};
  errorMsg : Array<string> = [];

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    const idCategory = this.activatedRoute.snapshot.params['idCategory'];
    if(idCategory){
      this.categoryService.findById(idCategory)
      .subscribe(cat => {
        this.categoryDto = cat;
      });
    }
  }

  cancelClick(): void{
    this.router.navigate(['categories']);
  }

  enregistrerCategory(): void {
    this.categoryService.enregistrerCategory(this.categoryDto)
    .subscribe(res => {
      this.router.navigate(['categories']);
    }, error => {
      this.errorMsg = error.error.errors;
    });
  }
}
