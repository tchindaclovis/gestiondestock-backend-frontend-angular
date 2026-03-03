import { Injectable } from '@angular/core';
import {EntrepriseDto, EntreprisesService} from "../../../gs-api/src";
import {Observable, ObservableInput} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class EntrepriseService {

  constructor(
    private entreprisesService: EntreprisesService
  ) { }

  sinscrire(entreprise: EntrepriseDto): Observable<EntrepriseDto>{
    return this.entreprisesService.save3(entreprise);
  }

}
