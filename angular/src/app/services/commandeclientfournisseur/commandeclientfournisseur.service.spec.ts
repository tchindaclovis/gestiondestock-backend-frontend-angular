import { TestBed } from '@angular/core/testing';

import { CommandeclientfournisseurService } from './commandeclientfournisseur.service';


describe('CommandeclientfournisseurService', () => {
  let service: CommandeclientfournisseurService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommandeclientfournisseurService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
