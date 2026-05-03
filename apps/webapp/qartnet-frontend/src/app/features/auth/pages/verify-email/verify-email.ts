import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [RouterLink, ButtonModule],
  templateUrl: './verify-email.html',
})
export class AuthVerifyEmailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  state: 'loading' | 'success' | 'error' = 'loading';
  errorMessage = '';

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.state = 'error';
      this.errorMessage = 'No verification token found in the URL.';
      return;
    }
    this.authService.verifyEmail(token).subscribe({
      next: () => (this.state = 'success'),
      error: (err) => {
        this.state = 'error';
        this.errorMessage = err.userMessage ?? 'Invalid or expired verification link.';
      },
    });
  }
}
