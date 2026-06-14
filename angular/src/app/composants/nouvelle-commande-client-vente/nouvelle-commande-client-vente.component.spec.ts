import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NouvelleCommandeClientVenteComponent } from './nouvelle-commande-client-vente.component';

describe('NouvelleCommandeClientVenteComponent', () => {
  let component: NouvelleCommandeClientVenteComponent;
  let fixture: ComponentFixture<NouvelleCommandeClientVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NouvelleCommandeClientVenteComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NouvelleCommandeClientVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
