import { Routes } from '@angular/router';

export const MESSAGING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/messaging-home/messaging-home').then((m) => m.MessagingHomeComponent),
  },
  {
    path: ':conversationPublicId',
    loadComponent: () =>
      import('./pages/messaging-home/messaging-home').then((m) => m.MessagingHomeComponent),
  },
];
