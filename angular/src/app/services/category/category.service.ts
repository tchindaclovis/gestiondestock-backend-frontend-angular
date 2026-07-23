import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {CategoriesService, CategoryDto} from "../../../gs-api/src";
import {Observable, of} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  constructor(
    private userService:UserService,
    private categoriesService: CategoriesService,
    private http: HttpClient                  // <--- Vérifie bien le "private" ici
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
    return this.categoriesService.delete8(idCategorie)
    }
    return of();  // ie je vais renvoyer un observable vide
  }

  getLastCodeCategory(): Observable<string> {
    // On récupère l'URL de base depuis le service généré ou on la définit
    const url = 'http://localhost:8081/gestiondestock/v1/categories/lastcodecategory';

    // L'option { responseType: 'text' } est CRUCIALRE ici
    return this.http.get(url, { responseType: 'text' });
  }
}
