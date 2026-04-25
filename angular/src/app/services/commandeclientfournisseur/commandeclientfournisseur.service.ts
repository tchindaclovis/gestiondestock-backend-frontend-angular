import { Injectable } from '@angular/core';
import {
  ArticleDto,
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
    private commandeClientsService: CommandeClientsService,
    private commandeFournisseursService: CommandeFournisseursService,
    private userService: UserService
  ) { }


  enregistrerCommandeClient(commandeClientDto: CommandeClientDto)
    : Observable<CommandeClientDto> {
    commandeClientDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeClientsService.save4(commandeClientDto);
  }


  enregistrerCommandeFournisseur(commandeFournisseurDto: CommandeFournisseurDto)
    : Observable<CommandeFournisseurDto> {
    commandeFournisseurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeFournisseursService.save5(commandeFournisseurDto);
  }


  // findAllCommandesClient(): Observable<CommandeClientDto[]> {
  //   return this.commandeClientService.findAll5();
  // }
  findAllCommandesClient(): Observable<CommandeClientDto[]> {
    return this.commandeClientsService.findAll4().pipe(
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


  findAllCommandeClientByIdEntreprise(idEntreprise: number): Observable<CommandeClientDto[]> {
    if (idEntreprise) {
      return this.commandeClientsService.findAllCommandeClientByIdEntreprise(idEntreprise);
    }
    return of([]);
  }



  // findAllCommandesFournisseur(): Observable<CommandeFournisseurDto[]> {
  //   return this.commandeFournisseurService.findAll4();
  // }
  findAllCommandesFournisseur(): Observable<CommandeFournisseurDto[]> {
    return this.commandeFournisseursService.findAll5().pipe(
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


  findAllCommandeFournisseurByIdEntreprise(idEntreprise: number): Observable<CommandeFournisseurDto[]> {
    if (idEntreprise) {
      return this.commandeFournisseursService.findAllCommandeFournisseurByIdEntreprise(idEntreprise);
    }
    return of([]);
  }


  // findAllLigneCommandesClient(idCmd?: number): Observable<LigneCommandeClientDto[]> {
  //   if (idCmd) {
  //     return this.commandeClientService.findAllLignesCommandesClientByCommandeClientId(idCmd);
  //   }
  //   return of();
  // }
  findAllLigneCommandesClientByCommande(idCmd?: number): Observable<LigneCommandeClientDto[]> {
    return this.commandeClientsService
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
  findAllLigneCommandesFournisseurByCommande(idCmd?: number): Observable<LigneCommandeFournisseurDto[]> {
    return this.commandeFournisseursService
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
    return this.commandeClientsService.findById4(id);
  }

  findCommandeFournisseurById(id: number): Observable<CommandeFournisseurDto> {
    return this.commandeFournisseursService.findById5(id);
  }


  deleteCommandeClient(idClient: number): Observable<any>{
    if(idClient){
      return this.commandeClientsService.delete4(idClient);
    }
    return of();
  }

  deleteCommandeFournisseur(idFournisseur: number): Observable<any>{
    if(idFournisseur){
      return this.commandeFournisseursService.delete5(idFournisseur);
    }
    return of();
  }

  getLastCodeCommandeClient(): Observable<string> {
    return this.commandeClientsService.getLastCodeCommandeClient();
  }

  getLastCodeCommandeFournisseur(): Observable<string> {
    return this.commandeFournisseursService.getLastCodeCommandeFournisseur();
  }
}

