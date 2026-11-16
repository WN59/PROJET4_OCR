import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { Session } from '../models/session.interface';
import { SessionApiService } from './session-api.service';

describe('SessionApiService', () => {
  let sessionApiService: SessionApiService;
  let httpMock: HttpTestingController;

  const mockSession: Session = {
    id: 1,
    name: 'Yoga débutant',
    description: 'Séance pour débutants',
    date: new Date('2026-01-15'),
    teacher_id: 2,
    users: [1, 3],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    sessionApiService = TestBed.inject(SessionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(sessionApiService).toBeTruthy();
  });

  it('should get all sessions', () => {
    const mockSessions = [mockSession];

    sessionApiService.all().subscribe((sessions) => {
      expect(sessions).toEqual(mockSessions);
    });

    const req = httpMock.expectOne('/api/session');
    expect(req.request.method).toBe('GET');
    req.flush(mockSessions);
  });

  it('should get session detail', () => {
    sessionApiService.detail('1').subscribe((session) => {
      expect(session).toEqual(mockSession);
    });

    const req = httpMock.expectOne('/api/session/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockSession);
  });

  it('should create a session', () => {
    sessionApiService.create(mockSession).subscribe((session) => {
      expect(session).toEqual(mockSession);
    });

    const req = httpMock.expectOne('/api/session');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockSession);
    req.flush(mockSession);
  });

  it('should update a session', () => {
    const updatedSession = { ...mockSession, name: 'Yoga avancé' };

    sessionApiService.update('1', updatedSession).subscribe((session) => {
      expect(session).toEqual(updatedSession);
    });

    const req = httpMock.expectOne('/api/session/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedSession);
    req.flush(updatedSession);
  });

  it('should delete a session', () => {
    sessionApiService.delete('1').subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne('/api/session/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should participate to a session', () => {
    sessionApiService.participate('1', '3').subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne('/api/session/1/participate/3');
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should unparticipate from a session', () => {
    sessionApiService.unParticipate('1', '3').subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne('/api/session/1/participate/3');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
