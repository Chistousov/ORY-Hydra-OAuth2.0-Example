import { TestBed } from '@angular/core/testing';

import { SaveFutureRedirectService } from './save-future-redirect.service';

describe('SaveFutureRedirectService', () => {
  let service: SaveFutureRedirectService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SaveFutureRedirectService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
