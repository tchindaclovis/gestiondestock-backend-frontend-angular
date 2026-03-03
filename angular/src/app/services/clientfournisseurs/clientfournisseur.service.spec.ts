import { TestBed } from '@angular/core/testing';

import { ClientfournisseurService } from './clientfournisseur.service';

describe('ClientfournisseursService', () => {
  let service: ClientfournisseurService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClientfournisseurService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
