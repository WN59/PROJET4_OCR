import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { of } from 'rxjs';
import { Session } from 'src/app/core/models/session.interface';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { Teacher } from 'src/app/core/models/teacher.interface';
import { SessionApiService } from 'src/app/core/service/session-api.service';
import { SessionService } from 'src/app/core/service/session.service';
import { TeacherService } from 'src/app/core/service/teacher.service';

import { DetailComponent } from './detail.component';

describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let sessionApiService: {
    detail: jest.Mock;
    delete: jest.Mock;
    participate: jest.Mock;
    unParticipate: jest.Mock;
  };
  let teacherService: { detail: jest.Mock };
  let matSnackBar: { open: jest.Mock };
  let router: Router;

  const mockSession: Session = {
    id: 1,
    name: 'Yoga débutant',
    description: 'Séance pour débutants',
    date: new Date('2026-01-15'),
    teacher_id: 2,
    users: [1],
    createdAt: new Date('2026-01-01'),
    updatedAt: new Date('2026-01-02'),
  };

  const mockTeacher: Teacher = {
    id: 2,
    firstName: 'Margot',
    lastName: 'DELAHAYE',
    createdAt: new Date('2026-01-01'),
    updatedAt: new Date('2026-01-01'),
  };

  const mockAdmin: SessionInformation = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'admin@test.com',
    firstName: 'Admin',
    lastName: 'User',
    admin: true,
  };

  const mockUser: SessionInformation = {
    ...mockAdmin,
    admin: false,
  };

  async function setup(
    sessionInformation: SessionInformation | undefined,
    session: Session = mockSession
  ) {
    TestBed.resetTestingModule();

    sessionApiService = {
      detail: jest.fn().mockReturnValue(of(session)),
      delete: jest.fn().mockReturnValue(of(null)),
      participate: jest.fn().mockReturnValue(of(null)),
      unParticipate: jest.fn().mockReturnValue(of(null)),
    };
    teacherService = {
      detail: jest.fn().mockReturnValue(of(mockTeacher)),
    };
    matSnackBar = {
      open: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [DetailComponent],
      providers: [
        provideRouter([]),
        { provide: SessionService, useValue: { sessionInformation } },
        { provide: SessionApiService, useValue: sessionApiService },
        { provide: TeacherService, useValue: teacherService },
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

    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', async () => {
    await setup(mockAdmin);
    expect(component).toBeTruthy();
  });

  it('should fallback when session information is missing', async () => {
    await setup(undefined);

    expect(component.isAdmin).toBe(false);
    expect(component.userId).toBe('');
  });

  it('should display session information', async () => {
    await setup(mockAdmin);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Yoga Débutant');
    expect(compiled.textContent).toContain('Séance pour débutants');
    expect(compiled.textContent).toContain('Margot');
    expect(compiled.textContent).toContain('DELAHAYE');
    expect(component.session).toEqual(mockSession);
    expect(component.teacher).toEqual(mockTeacher);
  });

  it('should show Delete button when user is admin', async () => {
    await setup(mockAdmin);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(component.isAdmin).toBe(true);
    expect(compiled.textContent).toContain('Delete');
    expect(compiled.textContent).not.toContain('Participate');
  });

  it('should hide Delete button when user is not admin', async () => {
    await setup(mockUser, { ...mockSession, users: [] });

    const compiled = fixture.nativeElement as HTMLElement;
    expect(component.isAdmin).toBe(false);
    expect(compiled.textContent).not.toContain('Delete');
    expect(compiled.textContent).toContain('Participate');
  });

  it('should delete the session and navigate to sessions', async () => {
    await setup(mockAdmin);

    component.delete();

    expect(sessionApiService.delete).toHaveBeenCalledWith('1');
    expect(matSnackBar.open).toHaveBeenCalledWith('Session deleted !', 'Close', { duration: 3000 });
    expect(router.navigate).toHaveBeenCalledWith(['sessions']);
  });

  it('should participate to a session', async () => {
    await setup(mockUser, { ...mockSession, users: [] });

    component.participate();

    expect(sessionApiService.participate).toHaveBeenCalledWith('1', '1');
    expect(sessionApiService.detail).toHaveBeenCalled();
  });

  it('should unparticipate from a session', async () => {
    await setup(mockUser);

    expect(component.isParticipate).toBe(true);
    component.unParticipate();

    expect(sessionApiService.unParticipate).toHaveBeenCalledWith('1', '1');
  });
});
