import { TestBed } from '@angular/core/testing';

import { LigneventeService } from './lignevente.service';

describe('LigneventeService', () => {
  let service: LigneventeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LigneventeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
