import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PasswordChangeRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  request: PasswordChangeRequest = { oldPassword: '', newPassword: '' };
  confirmPassword = '';
  error = '';
  success = '';

  constructor(private authService: AuthService, private router: Router) {}

  changePassword(): void {
    this.error = '';
    this.success = '';

    if (!this.request.oldPassword || !this.request.newPassword) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    if (this.request.newPassword.length < 6) {
      this.error = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    if (this.confirmPassword !== this.request.newPassword) {
      this.error = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.authService.changePassword({
      oldPassword: this.request.oldPassword,
      newPassword: this.request.newPassword
    }).subscribe({
      next: () => {
        this.success = 'Mot de passe modifié avec succès';
        setTimeout(() => this.router.navigate(['/reports']), 1500);
      },
      error: (err) => this.error = err.error?.error || 'Erreur lors de la modification'
    });
  }
}
