import { Injectable } from '@angular/core';
import {
  LigneVenteDto,
  LigneVentesService
} from "../../../gs-api/src";
import {Observable, of} from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class LigneVenteService {

  constructor(
    private ligneVentesService: LigneVentesService,
  ) { }

  findAllLigneVenteByIdEntreprise(idEntreprise: number): Observable<LigneVenteDto[]> {
    if (idEntreprise) {
      return this.ligneVentesService.findAllLigneVenteByIdEntreprise(idEntreprise);
    }
    return of([]);
  }
}
