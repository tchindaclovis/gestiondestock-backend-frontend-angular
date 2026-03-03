import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailMvtstockComponent } from './detail-mvtstock.component';

describe('DetailMvtstockComponent', () => {
  let component: DetailMvtstockComponent;
  let fixture: ComponentFixture<DetailMvtstockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DetailMvtstockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailMvtstockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
