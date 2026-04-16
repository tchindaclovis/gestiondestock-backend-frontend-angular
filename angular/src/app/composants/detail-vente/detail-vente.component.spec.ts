import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailVenteComponent } from './detail-vente.component';

describe('DetailVenteComponent', () => {
  let component: DetailVenteComponent;
  let fixture: ComponentFixture<DetailVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DetailVenteComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
