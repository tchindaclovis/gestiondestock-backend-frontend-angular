import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {
  ArticleDto,
  ArticlesService,
  CommandeClientDto,
  LigneCommandeClientDto, LigneVenteDto, UtilisateurDto,
  VenteDto,
  VentesService
} from "../../../gs-api/src";
import {from, map, Observable, of, switchMap} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class VenteService {

  constructor(
    private userService: UserService,
    private ventesService: VentesService
  ) { }

  enregistrerVente(venteDto: VenteDto): Observable<VenteDto>{
    venteDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.ventesService.save(venteDto);
  }

  findVenteById(id: number): Observable<VenteDto> {
    return this.ventesService.findById(id);
  }

  findAllVentes(): Observable<VenteDto[]> {
    return this.ventesService.findAll().pipe(
      switchMap((data: any) => {
        if (data instanceof Blob) {
          return from(data.text()).pipe(
            map(text => JSON.parse(text) as VenteDto[])
          );
        }
        return of(data);
      })
    );
  }


  findAllVenteByIdEntreprise(idEntreprise: number): Observable<VenteDto[]> {
    if (idEntreprise) {
      return this.ventesService.findAllVenteByIdEntreprise(idEntreprise);
    }
    return of([]);
  }


  findAllLigneVenteByVentes(idVte?: number): Observable<LigneVenteDto[]> {
    return this.ventesService
      .findAllLignesVentesByVenteId(idVte!)
      .pipe(
        switchMap((data: any) => {
          if (data instanceof Blob) {
            return from(data.text()).pipe(
              map(text => JSON.parse(text) as LigneVenteDto[])
            );
          }
          return of(data);
        })
      );
  }

  deleteVente(idClient: number): Observable<any>{
    if(idClient){
      return this.ventesService._delete(idClient);
    }
    return of();
  }

  getLastCodeVente(): Observable<string> {
    return this.ventesService.getLastCodeVente();
  }
}
