import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { LoginRequest } from '../models/loginRequest.interface';
import { RegisterRequest } from '../models/registerRequest.interface';
import { SessionInformation } from '../models/sessionInformation.interface';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let authService: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    authService = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(authService).toBeTruthy();
  });

  it('should call login endpoint', () => {
    const loginRequest: LoginRequest = {
      email: 'yoga@test.com',
      password: 'password123',
    };

    const mockResponse: SessionInformation = {
      token: 'fake-token',
      type: 'Bearer',
      id: 1,
      username: 'yoga@test.com',
      firstName: 'Yoga',
      lastName: 'Studio',
      admin: false,
    };

    authService.login(loginRequest).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
    req.flush(mockResponse);
  });

  it('should call register endpoint', () => {
    const registerRequest: RegisterRequest = {
      email: 'yoga@test.com',
      firstName: 'Yoga',
      lastName: 'Studio',
      password: 'password123',
    };

    const mockResponse = { message: 'User registered successfully!' };

    authService.register(registerRequest).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerRequest);
    req.flush(mockResponse);
  });
});
