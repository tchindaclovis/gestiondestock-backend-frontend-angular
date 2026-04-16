import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppercuVenteComponent } from './appercu-vente.component';

describe('AppercuVenteComponent', () => {
  let component: AppercuVenteComponent;
  let fixture: ComponentFixture<AppercuVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppercuVenteComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppercuVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
