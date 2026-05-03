import { Routes } from '@angular/router';

export const FORUM_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/forum-home/forum-home').then((m) => m.ForumHomeComponent),
  },
  {
    path: 'threads/new',
    loadComponent: () =>
      import('./pages/create-thread/create-thread').then((m) => m.CreateThreadComponent),
  },
  {
    path: 'threads',
    loadComponent: () =>
      import('./pages/thread-list/thread-list').then((m) => m.ThreadListComponent),
  },
  {
    path: 'threads/:publicId',
    loadComponent: () =>
      import('./pages/thread-detail/thread-detail').then((m) => m.ThreadDetailComponent),
  },
];
