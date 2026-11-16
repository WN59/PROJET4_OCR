import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { of, throwError } from 'rxjs';
import { AuthService } from 'src/app/core/service/auth.service';

import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: { register: jest.Mock };
  let router: { navigate: jest.Mock };

  beforeEach(async () => {
    authService = {
      register: jest.fn(),
    };
    router = {
      navigate: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [
        RegisterComponent,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
      ],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should disable submit when required fields are missing', () => {
    const submitButton: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');

    expect(component.form.invalid).toBe(true);
    expect(submitButton.disabled).toBe(true);
  });

  it('should register and navigate to login on success', () => {
    authService.register.mockReturnValue(of({ message: 'User registered!' }));

    component.form.setValue({
      email: 'yoga@test.com',
      firstName: 'Yoga',
      lastName: 'Studio',
      password: 'password123',
    });
    component.submit();

    expect(authService.register).toHaveBeenCalledWith({
      email: 'yoga@test.com',
      firstName: 'Yoga',
      lastName: 'Studio',
      password: 'password123',
    });
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    expect(component.onError).toBe(false);
  });

  it('should display an error when register fails', () => {
    authService.register.mockReturnValue(throwError(() => new Error('Conflict')));

    component.form.setValue({
      email: 'yoga@test.com',
      firstName: 'Yoga',
      lastName: 'Studio',
      password: 'password123',
    });
    component.submit();
    fixture.detectChanges();

    expect(component.onError).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('.error').textContent).toContain('An error occurred');
  });
});
