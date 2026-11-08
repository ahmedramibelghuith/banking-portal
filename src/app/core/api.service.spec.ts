import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api.service';
describe('Transfer wire contract', () => {
  it('sends large IDs and money as strings with the caller request key', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(ApiService)
      .transfer('1', '9007199254740993', '10.25', 'a6c2eb63-923f-4b2c-af82-dd5247dc2552')
      .subscribe();
    const request = http.expectOne('/api/operations/virement');
    expect(request.request.body).toEqual({
      compteSourceId: '1',
      compteDestinataireId: '9007199254740993',
      montant: '10.25',
    });
    expect(request.request.headers.get('Idempotency-Key')).toBe(
      'a6c2eb63-923f-4b2c-af82-dd5247dc2552',
    );
    request.flush(null);
    http.verify();
  });
});
