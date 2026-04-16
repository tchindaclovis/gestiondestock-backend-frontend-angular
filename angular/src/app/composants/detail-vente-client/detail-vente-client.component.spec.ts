import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailVenteClientComponent } from './detail-vente-client.component';

describe('DetailVenteClientComponent', () => {
  let component: DetailVenteClientComponent;
  let fixture: ComponentFixture<DetailVenteClientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DetailVenteClientComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailVenteClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
