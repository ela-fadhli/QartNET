import { Routes } from '@angular/router';

export const FORUM_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/forum-home/forum-home').then((m) => m.ForumListComponent),
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./pages/create-forum/create-forum').then((m) => m.CreateForumComponent),
  },
  {
    path: 'threads/:publicId',
    loadComponent: () =>
      import('./pages/thread-detail/thread-detail').then((m) => m.ThreadDetailComponent),
  },
  {
    path: ':slug/threads/new',
    loadComponent: () =>
      import('./pages/create-thread/create-thread').then((m) => m.CreateThreadComponent),
  },
  {
    path: ':slug',
    loadComponent: () =>
      import('./pages/thread-list/thread-list').then((m) => m.ForumDetailComponent),
  },
];
