import { Routes } from '@angular/router';

export const PROFILE_ROUTES: Routes = [
  {
    path: 'me',
    loadComponent: () => import('./pages/my-profile/my-profile').then((m) => m.ProfileMyProfileComponent),
  },
];
