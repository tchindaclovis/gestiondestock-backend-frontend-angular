import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppercuClientFournisseurComponent } from './appercu-client-fournisseur.component';

describe('AppercuClientFournisseurComponent', () => {
  let component: AppercuClientFournisseurComponent;
  let fixture: ComponentFixture<AppercuClientFournisseurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppercuClientFournisseurComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppercuClientFournisseurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
