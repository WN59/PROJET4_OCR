import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { SessionService } from '../core/service/session.service';
import { unauthGuard } from './unauth.guard';

describe('unauthGuard', () => {
  let sessionService: { isLogged: boolean };
  let router: { createUrlTree: jest.Mock };

  beforeEach(() => {
    sessionService = { isLogged: false };
    router = {
      createUrlTree: jest.fn().mockReturnValue('sessions-tree'),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: SessionService, useValue: sessionService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('should allow access when user is not logged in', () => {
    sessionService.isLogged = false;

    const result = TestBed.runInInjectionContext(() => unauthGuard({} as any, {} as any));

    expect(result).toBe(true);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('should redirect to sessions when user is already logged in', () => {
    sessionService.isLogged = true;

    const result = TestBed.runInInjectionContext(() => unauthGuard({} as any, {} as any));

    expect(router.createUrlTree).toHaveBeenCalledWith(['sessions']);
    expect(result).toBe('sessions-tree');
  });
});
