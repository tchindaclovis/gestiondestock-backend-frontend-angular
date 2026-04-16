import { Injectable } from '@angular/core';
import {
  CommandeClientDto, CommandeClientsService,
  CommandeFournisseurDto, CommandeFournisseursService,
  LigneCommandeClientDto,
  LigneCommandeFournisseurDto
} from "../../../gs-api/src";
import {from, map, Observable, of, switchMap} from "rxjs";
import {UserService} from "../user/user.service";

@Injectable({
  providedIn: 'root'
})
export class CommandeclientfournisseurService {

  constructor(
    private commandeClientService: CommandeClientsService,
    private commandeFournisseurService: CommandeFournisseursService,
    private userService: UserService
  ) { }


  enregistrerCommandeClient(commandeClientDto: CommandeClientDto)
    : Observable<CommandeClientDto> {
    commandeClientDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeClientService.save5(commandeClientDto);
  }


  enregistrerCommandeFournisseur(commandeFournisseurDto: CommandeFournisseurDto)
    : Observable<CommandeFournisseurDto> {
    commandeFournisseurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeFournisseurService.save4(commandeFournisseurDto);
  }


  // findAllCommandesClient(): Observable<CommandeClientDto[]> {
  //   return this.commandeClientService.findAll5();
  // }
  findAllCommandesClient(): Observable<CommandeClientDto[]> {
    return this.commandeClientService.findAll5().pipe(
      switchMap((data: any) => {
        if (data instanceof Blob) {
          return from(data.text()).pipe(
            map(text => JSON.parse(text) as CommandeClientDto[])
          );
        }
        return of(data);
      })
    );
  }



  // findAllCommandesFournisseur(): Observable<CommandeFournisseurDto[]> {
  //   return this.commandeFournisseurService.findAll4();
  // }
  findAllCommandesFournisseur(): Observable<CommandeFournisseurDto[]> {
    return this.commandeFournisseurService.findAll4().pipe(
      switchMap((data: any) => {
        if (data instanceof Blob) {
          return from(data.text()).pipe(
            map(text => JSON.parse(text) as CommandeFournisseurDto[])
          );
        }
        return of(data);
      })
    );
  }


  // findAllLigneCommandesClient(idCmd?: number): Observable<LigneCommandeClientDto[]> {
  //   if (idCmd) {
  //     return this.commandeClientService.findAllLignesCommandesClientByCommandeClientId(idCmd);
  //   }
  //   return of();
  // }
  findAllLigneCommandesClient(idCmd?: number): Observable<LigneCommandeClientDto[]> {
    return this.commandeClientService
      .findAllLignesCommandesClientByCommandeClientId(idCmd!)
      .pipe(
        switchMap((data: any) => {
          if (data instanceof Blob) {
            return from(data.text()).pipe(
              map(text => JSON.parse(text) as LigneCommandeClientDto[])
            );
          }
          return of(data);
        })
      );
  }



  // findAllLigneCommandesFournisseur(idCmd?: number): Observable<LigneCommandeFournisseurDto[]> {
  //   if (idCmd) {
  //     return this.commandeFournisseurService.findAllLignesCommandesFournisseurByCommandeFournisseurId(idCmd);
  //   }
  //   return of();
  // }
  findAllLigneCommandesFournisseur(idCmd?: number): Observable<LigneCommandeFournisseurDto[]> {
    return this.commandeFournisseurService
      .findAllLignesCommandesFournisseurByCommandeFournisseurId(idCmd!)
      .pipe(
        switchMap((data: any) => {
          if (data instanceof Blob) {
            return from(data.text()).pipe(
              map(text => JSON.parse(text) as LigneCommandeFournisseurDto[])
            );
          }
          return of(data);
        })
      );
  }


  findCommandeClientById(id: number): Observable<CommandeClientDto> {
    return this.commandeClientService.findById5(id);
  }

  findCommandeFournisseurById(id: number): Observable<CommandeFournisseurDto> {
    return this.commandeFournisseurService.findById4(id);
  }


  deleteCommandeClient(idClient: number): Observable<any>{
    if(idClient){
      return this.commandeClientService.delete5(idClient);
    }
    return of();
  }

  deleteCommandeFournisseur(idFournisseur: number): Observable<any>{
    if(idFournisseur){
      return this.commandeFournisseurService.delete4(idFournisseur);
    }
    return of();
  }
}

