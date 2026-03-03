import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {CategoriesService, CategoryDto} from "../../../gs-api/src";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  constructor(
    private userService:UserService,
    private categoriesService: CategoriesService
  ) { }

  enregistrerCategory(categoryDto: CategoryDto): Observable<CategoryDto>{
    categoryDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.categoriesService.save7(categoryDto)
  }

  findAll(): Observable<CategoryDto[]>{
    return this.categoriesService.findAll7();
  }

  findById(idCategory: number): Observable<CategoryDto> {
    return this.categoriesService.findById7(idCategory);
  }

  delete(idCategorie?: number): Observable<any> {  //any coe pr dire que je peut renvoyer n'importe quoi
    if(idCategorie){
    return this.categoriesService.delete7(idCategorie)
    }
    return of();  // ie je vais renvoyer un observable vide
  }
}
