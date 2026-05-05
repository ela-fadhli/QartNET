import { Routes } from '@angular/router';

export const REPOSITORIES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/repository-list/repository-list').then((m) => m.RepositoryListComponent),
  },
  {
    path: ':owner/:name',
    loadComponent: () => import('./pages/repository-detail/repository-detail').then((m) => m.RepositoryDetailComponent),
  },
];
