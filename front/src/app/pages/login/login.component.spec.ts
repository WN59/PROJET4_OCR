/**
 * =============================================================================
 * FICHIER DE RÉFÉRENCE — Test d'intégration Jest (LoginComponent)
 * =============================================================================
 *
 * - Il assemble plusieurs couches : composant + formulaire + template (DOM)
 *   + services mockés + router.
 * - Il couvre le plan  "Login" : succès, erreur identifiants, champs requis.
 * - Il montre les concepts clés Jest/Angular Testing :
 *   describe / it / beforeEach / expect / jest.fn / mockReturnValue /
 *   of / throwError / TestBed / fixture / detectChanges / assertions DOM.
 *
 * Type de test : INTÉGRATION (front)
 *
 * Schéma AAA dans chaque it :
 *   Arrange = préparer  |  Act = agir  |  Assert = vérifier (expect)
 * =============================================================================
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
// Import explicite : évite l'erreur TypeScript "Cannot find namespace 'jest'"
import { expect, jest } from '@jest/globals';
// of = Observable de succès immédiat | throwError = Observable d'erreur immédiat
import { of, throwError } from 'rxjs';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { AuthService } from 'src/app/core/service/auth.service';
import { SessionService } from 'src/app/core/service/session.service';

import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  // Instance de la classe LoginComponent (logique TS)
  let component: LoginComponent;
  // "Enveloppe" de test : donne accès au composant + à son DOM
  let fixture: ComponentFixture<LoginComponent>;

  /**
   * Mocks = fausses dépendances contrôlées.
   * jest.Mock = type d'une fonction créée avec jest.fn()
   * On pourra :
   *  - décider ce qu'elles retournent (mockReturnValue)
   *  - vérifier si/avec quoi elles ont été appelées (toHaveBeenCalledWith)
   */
  let authService: { login: jest.Mock };
  let sessionService: { logIn: jest.Mock };
  let router: { navigate: jest.Mock };

  /** Données de test réutilisables (fake user renvoyé par l'API en cas de succès). */
  const mockSessionInformation: SessionInformation = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'yoga@test.com',
    firstName: 'Yoga',
    lastName: 'Studio',
    admin: false,
  };

  /**
   * beforeEach : s'exécute AVANT chaque it(...)
   * But : repartir d'un état propre (nouveaux mocks + nouveau composant).
   */
  beforeEach(async () => {
    // ---------- ARRANGE (partie commune) ----------
    // jest.fn() crée une fausse fonction "espion"
    authService = {
      login: jest.fn(),
    };
    sessionService = {
      logIn: jest.fn(),
    };
    router = {
      navigate: jest.fn(),
    };

    /**
     * TestBed = mini Angular pour les tests.
     * - imports : le composant standalone + modules UI nécessaires au template
     * - providers : on REMPLACE les vrais services par nos mocks (useValue)
     *   → quand LoginComponent fait inject(AuthService), il reçoit authService (fake)
     */
    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule, // formulaire réactif ([formGroup], formControlName)
      ],
      providers: [
        provideNoopAnimations(), // Material sans animations réelles (plus rapide / stable)
        { provide: AuthService, useValue: authService },
        { provide: SessionService, useValue: sessionService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    // Crée le composant dans l'environnement de test
    fixture = TestBed.createComponent(LoginComponent);
    // Raccourci vers l'instance TS
    component = fixture.componentInstance;
    // Applique le template HTML (sinon le DOM n'est pas à jour)
    fixture.detectChanges();
  });

  /** Smoke test : le composant se crée sans planter. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Plan Adrianne : "Erreur d'affichage si un champ obligatoire est manquant"
   * Ici le form est vide au départ → invalid → bouton Submit disabled.
   */
  it('should disable submit when required fields are missing', () => {
    // On lit le VRAI bouton du template (intégration DOM)
    const submitButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      'button[type="submit"]'
    );

    // Assert : côté logique formulaire
    expect(component.form.invalid).toBe(true);
    // Assert : côté HTML ([disabled]="form.invalid")
    expect(submitButton.disabled).toBe(true);
  });

  /**
   * Plan Adrianne : "Fonctionnalité de connexion" (happy path)
   * Chaîne testée = celle du vrai submit() :
   *   AuthService.login OK → SessionService.logIn → Router.navigate('/sessions')
   */
  it('should login and navigate to sessions on success', () => {
    // Arrange : quand login() sera appelé, il renverra un Observable de succès
    authService.login.mockReturnValue(of(mockSessionInformation));

    // Arrange : on remplit le formulaire comme un utilisateur
    component.form.setValue({
      email: 'yoga@test.com',
      password: 'password123',
    });

    // Act : on déclenche la méthode testée
    component.submit();

    // Assert : AuthService a bien reçu les credentials du form
    expect(authService.login).toHaveBeenCalledWith({
      email: 'yoga@test.com',
      password: 'password123',
    });
    // Assert : en cas de succès, on enregistre la session
    expect(sessionService.logIn).toHaveBeenCalledWith(mockSessionInformation);
    // Assert : puis on navigue vers la liste des sessions
    expect(router.navigate).toHaveBeenCalledWith(['/sessions']);
    // Assert : pas d'état d'erreur
    expect(component.onError).toBe(false);
  });

  /**
   * Plan Adrianne : "Gestion d'erreur en cas de mauvais login / password"
   * On simule une API qui échoue → le composant doit afficher l'erreur
   * et NE PAS connecter / NE PAS naviguer.
   */
  it('should display an error when login fails', () => {
    // Arrange : login() renvoie une erreur (mauvais identifiants côté API)
    authService.login.mockReturnValue(
      throwError(() => new Error('Unauthorized'))
    );

    component.form.setValue({
      email: 'yoga@test.com',
      password: 'wrong-password',
    });

    // Act
    component.submit();
    // Important : onError vient de passer à true → il faut rafraîchir le HTML
    // pour que le @if (onError) affiche <p class="error">...</p>
    fixture.detectChanges();

    // Assert : le flag d'erreur du composant
    expect(component.onError).toBe(true);
    // Assert : on n'a PAS enregistré de session
    expect(sessionService.logIn).not.toHaveBeenCalled();
    // Assert : on n'a PAS navigué
    expect(router.navigate).not.toHaveBeenCalled();
    // Assert : le message est bien visible dans le DOM
    expect(fixture.nativeElement.querySelector('.error').textContent).toContain(
      'An error occurred'
    );
  });
});
