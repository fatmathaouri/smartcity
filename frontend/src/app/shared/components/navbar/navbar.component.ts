import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthResponse } from '../../../core/models/user.model';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  user: AuthResponse | null = null;
  unreadCount = 0;
  isHome = false;

  constructor(
    public authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
      this.isHome = this.router.url === '/';
      this.refreshUser();
    });
  }

  ngOnInit(): void {
    this.refreshUser();
    if (this.authService.isLoggedIn()) {
      this.notificationService.getUnreadCount().subscribe(c => this.unreadCount = c);
    }
  }

  private refreshUser(): void {
    this.user = this.authService.getCurrentUser();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_MUNICIPALITY');
  }

  get isAgent(): boolean {
    return this.authService.hasRole('ROLE_MUNICIPAL_AGENT');
  }

  get isManager(): boolean {
    return this.authService.hasRole('ROLE_DEPARTMENT_MANAGER') || this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_MUNICIPALITY');
  }
}
