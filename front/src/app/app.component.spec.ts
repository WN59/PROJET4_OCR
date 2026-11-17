import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { provideRouter, Router } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from './core/service/auth.service';
import { SessionService } from './core/service/session.service';

import { AppComponent } from './app.component';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;
  let isLogged$: BehaviorSubject<boolean>;
  let sessionService: {
    $isLogged: jest.Mock;
    logOut: jest.Mock;
  };
  let router: Router;

  beforeEach(async () => {
    isLogged$ = new BehaviorSubject<boolean>(false);
    sessionService = {
      $isLogged: jest.fn().mockReturnValue(isLogged$.asObservable()),
      logOut: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [AppComponent, MatToolbarModule],
      providers: [
        provideRouter([]),
        { provide: SessionService, useValue: sessionService },
        { provide: AuthService, useValue: {} },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate');

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should expose the logged status from SessionService', (done) => {
    component.$isLogged().subscribe((isLogged) => {
      expect(isLogged).toBe(false);
      expect(sessionService.$isLogged).toHaveBeenCalled();
      done();
    });
  });

  it('should logout and navigate to home', () => {
    component.logout();

    expect(sessionService.logOut).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['']);
  });

  it('should display Login and Register when user is not logged in', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Login');
    expect(compiled.textContent).toContain('Register');
    expect(compiled.textContent).not.toContain('Logout');
  });

  it('should display Sessions, Account and Logout when user is logged in', () => {
    isLogged$.next(true);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Sessions');
    expect(compiled.textContent).toContain('Account');
    expect(compiled.textContent).toContain('Logout');
  });
});
