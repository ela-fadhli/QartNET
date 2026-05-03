import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-email-pending',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './email-pending.html',
})
export class AuthEmailPendingComponent {}
