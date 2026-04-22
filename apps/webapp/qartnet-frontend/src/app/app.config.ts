import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { definePreset } from '@primeuix/themes';

const MyPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#ebf5ff',
      100: '#d6ebff',
      200: '#add6ff',
      300: '#7ebfff',
      400: '#4da3ff',
      500: '#006edc', // rgb(0, 110, 220)
      600: '#005cc3',
      700: '#0049a3',
      800: '#003c85',
      900: '#00336b',
      950: '#00224d'
    }
  }
});

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    providePrimeNG({
      theme: {
        preset: MyPreset,
        options: {
          darkModeSelector: 'none'
        }
      }
    })
  ]
};
