import { TestBed } from '@angular/core/testing';

import { MvtstockService } from './mvtstock.service';

describe('MvtstockarticleService', () => {
  let service: MvtstockService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MvtstockService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
