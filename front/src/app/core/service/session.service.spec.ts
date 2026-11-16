import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { SessionInformation } from '../models/sessionInformation.interface';
import { SessionService } from './session.service';

describe('SessionService', () => {
  let sessionService: SessionService;

  const mockUser: SessionInformation = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'yoga@test.com',
    firstName: 'Yoga',
    lastName: 'Studio',
    admin: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    sessionService = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(sessionService).toBeTruthy();
  });

  it('should log in the user', () => {
    sessionService.logIn(mockUser);
    //console.log('sessionService.isLogged', sessionService.isLogged);
    //console.log('sessionService.sessionInformation', sessionService.sessionInformation);
    expect(sessionService.isLogged).toBe(true);
    expect(sessionService.sessionInformation).toEqual(mockUser);
  });

  it('should log out the user', () => {
    sessionService.logIn(mockUser);
    sessionService.logOut();
    //console.log('sessionService.isLogged', sessionService.isLogged);
    //console.log('sessionService.sessionInformation', sessionService.sessionInformation);
    expect(sessionService.isLogged).toBe(false);
    expect(sessionService.sessionInformation).toBeUndefined();
  });

  it('should emit the logged status through $isLogged', () => {
    const emittedValues: boolean[] = [];
    //console.log('emittedValues1', emittedValues);
    sessionService.$isLogged().subscribe((isLogged) => emittedValues.push(isLogged));
    //console.log('emittedValues2', emittedValues);
    sessionService.logIn(mockUser);
    //console.log('emittedValues3', emittedValues);
    sessionService.logOut();
    //console.log('emittedValues4', emittedValues)
    expect(emittedValues).toEqual([false, true, false]);
  });
});
