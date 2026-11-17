import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { of } from 'rxjs';
import { Session } from 'src/app/core/models/session.interface';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { Teacher } from 'src/app/core/models/teacher.interface';
import { SessionApiService } from 'src/app/core/service/session-api.service';
import { SessionService } from 'src/app/core/service/session.service';
import { TeacherService } from 'src/app/core/service/teacher.service';

import { FormComponent } from './form.component';

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let sessionApiService: {
    detail: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
  };
  let matSnackBar: { open: jest.Mock };
  let router: Router;

  const mockAdmin: SessionInformation = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'admin@test.com',
    firstName: 'Admin',
    lastName: 'User',
    admin: true,
  };

  const mockTeacher: Teacher = {
    id: 1,
    firstName: 'Margot',
    lastName: 'DELAHAYE',
    createdAt: new Date('2026-01-01'),
    updatedAt: new Date('2026-01-01'),
  };

  const mockSession: Session = {
    id: 1,
    name: 'Yoga débutant',
    description: 'Séance pour débutants',
    date: new Date('2026-01-15T00:00:00.000Z'),
    teacher_id: 1,
    users: [],
  };

  async function setup(options: {
    admin?: boolean;
    url?: string;
  } = {}) {
    TestBed.resetTestingModule();

    const sessionInformation = {
      ...mockAdmin,
      admin: options.admin ?? true,
    };
    const url = options.url ?? '/sessions/create';

    sessionApiService = {
      detail: jest.fn().mockReturnValue(of(mockSession)),
      create: jest.fn().mockReturnValue(of(mockSession)),
      update: jest.fn().mockReturnValue(of(mockSession)),
    };
    matSnackBar = {
      open: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [
        FormComponent,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
      ],
      providers: [
        provideNoopAnimations(),
        provideRouter([
          { path: 'sessions/create', component: FormComponent },
          { path: 'sessions/update/:id', component: FormComponent },
          { path: 'sessions', component: FormComponent },
        ]),
        { provide: SessionService, useValue: { sessionInformation } },
        { provide: SessionApiService, useValue: sessionApiService },
        { provide: TeacherService, useValue: { all: jest.fn().mockReturnValue(of([mockTeacher])) } },
        { provide: MatSnackBar, useValue: matSnackBar },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: jest.fn().mockReturnValue('1'),
              },
            },
          },
        },
      ],
    })
      .overrideProvider(MatSnackBar, { useValue: matSnackBar })
      .compileComponents();

    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate');
    await router.navigateByUrl(url);

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', async () => {
    await setup();
    expect(component).toBeTruthy();
  });

  it('should redirect non-admin users to sessions', async () => {
    await setup({ admin: false });

    expect(router.navigate).toHaveBeenCalledWith(['/sessions']);
  });

  it('should disable submit when required fields are missing', async () => {
    await setup();

    expect(component.sessionForm?.invalid).toBe(true);
    const submitButton: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(submitButton.disabled).toBe(true);
  });

  it('should create a session successfully', async () => {
    await setup({ url: '/sessions/create' });

    component.sessionForm?.setValue({
      name: 'Yoga débutant',
      date: '2026-01-15',
      teacher_id: 1,
      description: 'Séance pour débutants',
    });
    component.submit();

    expect(sessionApiService.create).toHaveBeenCalled();
    expect(matSnackBar.open).toHaveBeenCalledWith('Session created !', 'Close', { duration: 3000 });
    expect(router.navigate).toHaveBeenCalledWith(['sessions']);
  });

  it('should update a session successfully', async () => {
    await setup({ url: '/sessions/update/1' });

    expect(component.onUpdate).toBe(true);
    expect(sessionApiService.detail).toHaveBeenCalledWith('1');
    expect(component.sessionForm?.value.name).toBe('Yoga débutant');

    component.sessionForm?.setValue({
      name: 'Yoga avancé',
      date: '2026-01-20',
      teacher_id: 1,
      description: 'Séance avancée',
    });
    component.submit();

    expect(sessionApiService.update).toHaveBeenCalledWith('1', {
      name: 'Yoga avancé',
      date: '2026-01-20',
      teacher_id: 1,
      description: 'Séance avancée',
    });
    expect(matSnackBar.open).toHaveBeenCalledWith('Session updated !', 'Close', { duration: 3000 });
    expect(router.navigate).toHaveBeenCalledWith(['sessions']);
  });
});
