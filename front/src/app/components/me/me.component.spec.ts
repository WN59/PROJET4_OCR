import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter, Router } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { of } from 'rxjs';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { User } from 'src/app/core/models/user.interface';
import { SessionService } from 'src/app/core/service/session.service';
import { UserService } from 'src/app/core/service/user.service';

import { MeComponent } from './me.component';

describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let userService: {
    getById: jest.Mock;
    delete: jest.Mock;
  };
  let sessionService: {
    sessionInformation: SessionInformation;
    logOut: jest.Mock;
  };
  let matSnackBar: { open: jest.Mock };
  let router: Router;

  const mockSessionInformation: SessionInformation = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'yoga@test.com',
    firstName: 'Yoga',
    lastName: 'Studio',
    admin: false,
  };

  const mockUser: User = {
    id: 1,
    email: 'yoga@test.com',
    firstName: 'Yoga',
    lastName: 'Studio',
    admin: false,
    password: '',
    createdAt: new Date('2026-01-01'),
    updatedAt: new Date('2026-01-02'),
  };

  beforeEach(async () => {
    userService = {
      getById: jest.fn().mockReturnValue(of(mockUser)),
      delete: jest.fn().mockReturnValue(of(null)),
    };
    sessionService = {
      sessionInformation: mockSessionInformation,
      logOut: jest.fn(),
    };
    matSnackBar = {
      open: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MeComponent, MatCardModule, MatIconModule],
      providers: [
        provideRouter([]),
        { provide: SessionService, useValue: sessionService },
        { provide: UserService, useValue: userService },
        { provide: MatSnackBar, useValue: matSnackBar },
      ],
    })
      .overrideProvider(MatSnackBar, { useValue: matSnackBar })
      .compileComponents();

    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate');

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display user information', () => {
    expect(userService.getById).toHaveBeenCalledWith('1');
    expect(component.user).toEqual(mockUser);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Yoga');
    expect(compiled.textContent).toContain('STUDIO');
    expect(compiled.textContent).toContain('yoga@test.com');
  });

  it('should delete account, logout and navigate home', () => {
    component.delete();

    expect(userService.delete).toHaveBeenCalledWith('1');
    expect(matSnackBar.open).toHaveBeenCalledWith(
      'Your account has been deleted !',
      'Close',
      { duration: 3000 }
    );
    expect(sessionService.logOut).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});
