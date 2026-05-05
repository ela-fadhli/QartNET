import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'forum',
        loadChildren: () => import('./features/forum/forum.routes').then((m) => m.FORUM_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () => import('./features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
      {
      {
        path: 'messaging',
        loadChildren: () => import('./features/messaging/messaging.routes').then((m) => m.MESSAGING_ROUTES),
      },
      {
        path: 'projects',
        loadChildren: () => import('./features/projects/projects.routes').then((m) => m.PROJECTS_ROUTES),
      },
      {
        path: 'repositories',
        loadChildren: () => import('./features/repositories/repositories.routes').then((m) => m.REPOSITORIES_ROUTES),
      },
      { path: 'groups', loadComponent: () => import('./layouts/main-layout/main-layout').then(m => m.MainLayoutComponent) },
      { path: 'notifications', loadComponent: () => import('./layouts/main-layout/main-layout').then(m => m.MainLayoutComponent) },
    ],
  },
  {
    path: '',
    redirectTo: 'profile/me',
    pathMatch: 'full',
  },
];
