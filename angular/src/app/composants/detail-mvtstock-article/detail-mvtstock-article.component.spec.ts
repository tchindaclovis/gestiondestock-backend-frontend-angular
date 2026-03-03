import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailMvtstockArticleComponent } from './detail-mvtstock-article.component';

describe('DetailMvtstockArticleComponent', () => {
  let component: DetailMvtstockArticleComponent;
  let fixture: ComponentFixture<DetailMvtstockArticleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DetailMvtstockArticleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailMvtstockArticleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
