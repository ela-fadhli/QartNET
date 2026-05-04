import { Routes } from '@angular/router';

export const PROJECTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/project-list/project-list').then((m) => m.ProjectListComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/project-detail/project-detail').then((m) => m.ProjectDetailComponent),
  },
];
