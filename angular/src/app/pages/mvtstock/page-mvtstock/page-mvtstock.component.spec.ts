import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageMvtstockComponent } from './page-mvtstock.component';

describe('PageMvtstockComponent', () => {
  let component: PageMvtstockComponent;
  let fixture: ComponentFixture<PageMvtstockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PageMvtstockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PageMvtstockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
