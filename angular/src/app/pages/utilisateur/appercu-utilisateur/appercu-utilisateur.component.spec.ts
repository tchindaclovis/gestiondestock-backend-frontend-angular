import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppercuUtilisateurComponent } from './appercu-utilisateur.component';

describe('AppercuUtilisateurComponent', () => {
  let component: AppercuUtilisateurComponent;
  let fixture: ComponentFixture<AppercuUtilisateurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppercuUtilisateurComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppercuUtilisateurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
