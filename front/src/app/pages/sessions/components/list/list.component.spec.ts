import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { provideRouter } from '@angular/router';
import { expect, jest } from '@jest/globals';
import { of } from 'rxjs';
import { Session } from 'src/app/core/models/session.interface';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { SessionApiService } from 'src/app/core/service/session-api.service';
import { SessionService } from 'src/app/core/service/session.service';

import { ListComponent } from './list.component';

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;

  const mockSessions: Session[] = [
    {
      id: 1,
      name: 'Yoga débutant',
      description: 'Séance pour débutants',
      date: new Date('2026-01-15'),
      teacher_id: 1,
      users: [1],
    },
  ];

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
    username: 'user@test.com',
  };

  async function setup(sessionInformation: SessionInformation) {
    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [ListComponent, MatCardModule, MatIconModule],
      providers: [
        provideRouter([]),
        { provide: SessionService, useValue: { sessionInformation } },
        {
          provide: SessionApiService,
          useValue: {
            all: jest.fn().mockReturnValue(of(mockSessions)),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', async () => {
    await setup(mockAdmin);
    expect(component).toBeTruthy();
  });

  it('should display the list of sessions', async () => {
    await setup(mockAdmin);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Yoga débutant');
    expect(compiled.textContent).toContain('Séance pour débutants');
    expect(compiled.textContent).toContain('Detail');
  });

  it('should show Create and Edit buttons when user is admin', async () => {
    await setup(mockAdmin);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Create');
    expect(compiled.textContent).toContain('Edit');
  });

  it('should hide Create and Edit buttons when user is not admin', async () => {
    await setup(mockUser);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).not.toContain('Create');
    expect(compiled.textContent).not.toContain('Edit');
    expect(compiled.textContent).toContain('Detail');
  });
});
