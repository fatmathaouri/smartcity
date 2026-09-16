import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  request: RegisterRequest = { email: '', password: '', firstName: '', lastName: '', phone: '', address: '', city: '' };
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  register(): void {
    this.error = '';
    this.authService.register(this.request).subscribe({
      next: () => this.router.navigate(['/reports']),
      error: (err) => this.error = err.error?.error || 'Registration failed'
    });
  }
}
