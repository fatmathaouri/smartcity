import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return false;
    }

    if (this.authService.mustChangePassword() && state.url !== '/change-password') {
      this.router.navigate(['/change-password']);
      return false;
    }

    if (state.url === '/change-password' && !this.authService.mustChangePassword()) {
      this.router.navigate(['/dashboard']);
      return false;
    }

    const requiredRoles = route.data['roles'] as string[];
    if (requiredRoles && requiredRoles.length > 0) {
      const hasRole = requiredRoles.some(role => this.authService.hasRole(role));
      if (!hasRole) {
        this.router.navigate(['/dashboard']);
        return false;
      }
    }

    return true;
  }
}
