import { TestBed } from '@angular/core/testing';

import { LignecmdclientfournisseurService } from './lignecmdclientfournisseur.service';

describe('LignecmdclientfournisseurService', () => {
  let service: LignecmdclientfournisseurService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LignecmdclientfournisseurService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
