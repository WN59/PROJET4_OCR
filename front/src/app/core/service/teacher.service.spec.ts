import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { Teacher } from '../models/teacher.interface';
import { TeacherService } from './teacher.service';

describe('TeacherService', () => {
  let teacherService: TeacherService;
  let httpMock: HttpTestingController;

  const mockTeacher: Teacher = {
    id: 1,
    firstName: 'Margot',
    lastName: 'DELAHAYE',
    createdAt: new Date('2026-01-01'),
    updatedAt: new Date('2026-01-01'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    teacherService = TestBed.inject(TeacherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(teacherService).toBeTruthy();
  });

  it('should get all teachers', () => {
    const mockTeachers = [mockTeacher];

    teacherService.all().subscribe((teachers) => {
      expect(teachers).toEqual(mockTeachers);
    });

    const req = httpMock.expectOne('/api/teacher');
    expect(req.request.method).toBe('GET');
    req.flush(mockTeachers);
  });

  it('should get teacher detail', () => {
    teacherService.detail('1').subscribe((teacher) => {
      expect(teacher).toEqual(mockTeacher);
    });

    const req = httpMock.expectOne('/api/teacher/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockTeacher);
  });
});
