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
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CommandeclientfournisseurService {

  constructor(
    private commandeClientsService: CommandeClientsService,
    private commandeFournisseursService: CommandeFournisseursService,
    private userService: UserService,
    private http: HttpClient                  // <--- Vérifie bien le "private" ici
  ) { }


  // enregistrerCommandeClientImpact(
  //   commandeClientDto: CommandeClientDto, idCommande?: number, etatCommande?: string
  // ): Observable<CommandeClientDto> {
  //   const id = idCommande ? idCommande : 0;
  //   const etat = etatCommande ? etatCommande : 'PRO_FORMAT';
  //
  //   // On respecte l'ordre : ID, ETAT, DTO
  //   // Et on ajoute .pipe(map(...)) pour extraire le body
  //   return this.commandeClientsService.saveImpact(id, etat as any, commandeClientDto)
  //     .pipe(
  //       map((res: any) => res.body || res) // Récupère le body si c'est une HttpResponse
  //     );
  // }

  // enregistrerCommandeClient(
  //   commandeClientDto: CommandeClientDto, idCommande?: number, etatCommande?: string
  // ): Observable<CommandeClientDto> {
  //   // On injecte l'idEntreprise
  //   commandeClientDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
  //
  //   // On passe les 3 paramètres demandés par le Backend
  //   // Note: Assurez-vous que le service généré save4 accepte bien ces 3 arguments
  //   return this.commandeClientsService.save4(idCommande!, etatCommande as any, commandeClientDto, );
  // }

  enregistrerCommandeClient(commandeClientDto: CommandeClientDto )
    : Observable<CommandeClientDto> {
    commandeClientDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeClientsService.save5(commandeClientDto);
  }

  enregistrerCommandeClient1(commandeClientDto: CommandeClientDto )
    : Observable<CommandeClientDto> {
    commandeClientDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeClientsService.saveImpact1(commandeClientDto);
  }


  // enregistrerCommandeFournisseurImpact(
  //   commandeFournisseurDto: CommandeFournisseurDto, idCommande?: number, etatCommande?: string,
  // ): Observable<CommandeFournisseurDto> {
  //   const id = idCommande ? idCommande : 0;
  //   const etat = etatCommande ? etatCommande : 'PRO_FORMAT';
  //
  //   return this.commandeFournisseursService.saveImpact1(id, etat as any, commandeFournisseurDto)
  //     .pipe(
  //       map((res: any) => res.body || res)
  //     );
  // }

  // enregistrerCommandeFournisseur(
  //   commandeFournisseurDto: CommandeFournisseurDto, idCommande?: number, etatCommande?: string
  // ): Observable<CommandeFournisseurDto> {
  //   commandeFournisseurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
  //   return this.commandeFournisseursService.save5(idCommande!, etatCommande as any, commandeFournisseurDto
  //   );
  // }

  enregistrerCommandeFournisseur(commandeFournisseurDto: CommandeFournisseurDto)
    : Observable<CommandeFournisseurDto> {
    commandeFournisseurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeFournisseursService.save4(commandeFournisseurDto);
  }

  enregistrerCommandeFournisseur1(commandeFournisseurDto: CommandeFournisseurDto)
    : Observable<CommandeFournisseurDto> {
    commandeFournisseurDto.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id; //affecter l'idEntreprise à l'utilisateur
    return this.commandeFournisseursService.saveImpact(commandeFournisseurDto);
  }


  updateEtatCommandeClient(idCommande: number, etatCommande: string, dateConfirmation: string): Observable<CommandeClientDto> {
    if (idCommande && etatCommande) {
      // On appelle le service généré par Swagger.
      // Note : le nom de la méthode peut varier selon votre génération (ex: updateEtatCommande, patchEtat, etc.)
      return this.commandeClientsService.updateEtatCommande(idCommande, etatCommande as any, dateConfirmation);
    }
    return of();
  }

  updateEtatCommandeFournisseur(idCommande: number, etatCommande: string, dateConfirmation: string): Observable<CommandeFournisseurDto> {
    if (idCommande && etatCommande) {
      return this.commandeFournisseursService.updateEtatCommande1(idCommande, etatCommande as any, dateConfirmation);
    }
    return of();
  }


  // findAllCommandesClient(): Observable<CommandeClientDto[]> {
  //   return this.commandeClientService.findAll5();
  // }
  findAllCommandesClient(): Observable<CommandeClientDto[]> {
    return this.commandeClientsService.findAll5().pipe(
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
      return this.commandeClientsService.findAllCommandeClientByIdEntreprise(idEntreprise).pipe(
        map(data => {
          // Si les données arrivent déjà sous forme d'objet (JSON), on les retourne telles quelles
          return data;
        })
      );
      // return this.commandeClientsService.findAllCommandeClientByIdEntreprise(idEntreprise);
    }
    return of([]);
  }

  findAllCommandeFournisseurByIdEntreprise(idEntreprise: number): Observable<CommandeFournisseurDto[]> {
    if (idEntreprise) {
      return this.commandeFournisseursService.findAllCommandeFournisseurByIdEntreprise(idEntreprise).pipe(
        map(data => {
          // Si les données arrivent déjà sous forme d'objet (JSON), on les retourne telles quelles
          return data;
        })
      );
      // return this.commandeFournisseursService.findAllCommandeFournisseurByIdEntreprise(idEntreprise);
    }
    return of([]);
  }


  // findAllCommandesFournisseur(): Observable<CommandeFournisseurDto[]> {
  //   return this.commandeFournisseurService.findAll4();
  // }
  findAllCommandesFournisseur(): Observable<CommandeFournisseurDto[]> {
    return this.commandeFournisseursService.findAll4().pipe(
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
    return this.commandeClientsService.findById5(id);
  }

  findCommandeFournisseurById(id: number): Observable<CommandeFournisseurDto> {
    return this.commandeFournisseursService.findById4(id);
  }


  deleteCommandeClient(idClient: number): Observable<any>{
    if(idClient){
      return this.commandeClientsService.delete5(idClient);
    }
    return of();
  }

  deleteCommandeFournisseur(idFournisseur: number): Observable<any>{
    if(idFournisseur){
      return this.commandeFournisseursService.delete4(idFournisseur);
    }
    return of();
  }

  getLastCodeCommandeClient(): Observable<string> {
    // On récupère l'URL de base depuis le service généré ou on la définit
    const url = 'http://localhost:8081/gestiondestock/v1/commandeclients/lastcodecommandeclient';

    // L'option { responseType: 'text' } est CRUCIALRE ici
    return this.http.get(url, { responseType: 'text' });
  }

  getLastCodeCommandeFournisseur(): Observable<string> {
    // On récupère l'URL de base depuis le service généré ou on la définit
    const url = 'http://localhost:8081/gestiondestock/v1/commandefournisseurs/lastcodecommandefournisseur';

    // L'option { responseType: 'text' } est CRUCIALRE ici
    return this.http.get(url, { responseType: 'text' });
  }
}

