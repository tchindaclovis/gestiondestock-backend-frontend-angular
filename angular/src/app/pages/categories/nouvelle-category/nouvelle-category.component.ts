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
    }else {
      this.categoryService.getLastCodeCategory().subscribe({
        next: async (res: any) => { // Ajoutez 'async' ici
          let rawValue = res;
          // Si la réponse est un Blob, on extrait son contenu textuel
          if (res instanceof Blob) {
            rawValue = await res.text();
          }
          console.log('Valeur textuelle extraite :', res); // Devrait afficher "CAT013"
          this.categoryDto.code = this.genererProchainCode(res);
        },
        error: (err) => {
          console.error('Erreur API :', err);
          this.categoryDto.code = 'CAT001';
        }
      });
    }
  }

  private genererProchainCode(lastCode: any): string {
    console.log('Type de lastCode :', typeof lastCode);
    console.log('Valeur brute de lastCode :', lastCode);
    // 1. Conversion en string et nettoyage radical (supprime guillemets, espaces, retours à la ligne)
    const cleanCode = String(lastCode).replace(/["\s\n\r]/g, '');

    // 2. Extraction de TOUS les chiffres présents dans la chaîne
    // On cherche une suite de chiffres (\d+)
    const match = cleanCode.match(/\d+/);

    let nextNumber = 999; // Valeur par défaut si aucun chiffre n'est trouvé

    if (match && match[0]) {
      // 3. Conversion de la partie trouvée (ex: "013") en nombre et incrémentation
      nextNumber = parseInt(match[0], 10) + 1;
    }

    // 4. Formatage : "ART" + nombre formaté sur 4 positions (Milliers, Centaines, Dizaines, Unités)
    // padStart(4, '0') transforme 14 en "0014"
    const formattedNumber = nextNumber.toString().padStart(3, '0');

    return `CAT${formattedNumber}`;
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
