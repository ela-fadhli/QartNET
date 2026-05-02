import { Component, inject, OnInit, signal, viewChild } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Menu } from 'primeng/menu';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../features/auth/services/auth.service';
import { WebSocketService } from '../../core/services/websocket.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    Menu,
    AvatarModule,
    BadgeModule,
    ButtonModule,
    InputTextModule,
  ],
  templateUrl: './main-layout.html',
  styles: [`
    .nav-active {
      color: #C9A84C;
      background-color: rgba(201, 168, 76, 0.1);
      font-weight: 500;
    }
    .nav-active i { color: #C9A84C; }
  `],
})
export class MainLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private wsService = inject(WebSocketService);
  private router = inject(Router);

  searchQuery = signal('');
  userMenu = viewChild<Menu>('userMenu');

  userMenuItems: MenuItem[] = [
    { label: 'My Profile', icon: 'pi pi-user', routerLink: '/profile/me' },
    { separator: true },
    { label: 'Sign out', icon: 'pi pi-sign-out', command: () => this.logout() },
  ];

  ngOnInit(): void {
    const token = this.authService.getToken();
    if (token && !this.wsService.connected) {
      this.wsService.connect(token);
    }
  }

  onSearch(event: KeyboardEvent): void {
    if (event.key === 'Enter' && this.searchQuery().trim()) {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery().trim() } });
    }
  }

  toggleUserMenu(event: Event): void {
    this.userMenu()?.toggle(event);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/sign-in']);
  }
}
