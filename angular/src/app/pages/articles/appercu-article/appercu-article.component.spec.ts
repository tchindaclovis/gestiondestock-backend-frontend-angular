import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppercuArticleComponent } from './appercu-article.component';

describe('AppercuArticleComponent', () => {
  let component: AppercuArticleComponent;
  let fixture: ComponentFixture<AppercuArticleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppercuArticleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppercuArticleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
