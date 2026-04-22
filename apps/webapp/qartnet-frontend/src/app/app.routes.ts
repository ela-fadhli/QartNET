import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { 
    path: 'login', 
    loadComponent: () => import('./features/auth/login/login.component').then(c => c.LoginComponent)
  },
  { 
    path: 'register', 
    loadComponent: () => import('./features/auth/register/register.component').then(c => c.RegisterComponent)
  },
  {
    path: 'admin',
    redirectTo: 'admin/login',
    pathMatch: 'full'
  },
  {
    path: 'admin/login',
    loadComponent: () => import('./features/admin/login/admin-login.component').then(c => c.AdminLoginComponent)
  },
  {
    path: 'admin/dashboard',
    loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(c => c.DashboardComponent)
  }
];
