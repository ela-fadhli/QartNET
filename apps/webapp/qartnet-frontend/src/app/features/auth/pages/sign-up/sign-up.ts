import { Component, inject } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const confirm = control.get('confirmPassword');
  if (password && confirm && password.value !== confirm.value) {
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
  ],
  templateUrl: './sign-up.html',
})
export class AuthSignUpComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = false;
  errorMessage: string | null = null;

  form = this.fb.group(
    {
      firstName:       ['', [Validators.required, Validators.maxLength(50)]],
      lastName:        ['', [Validators.required, Validators.maxLength(50)]],
      dateOfBirth:     [''],
      phoneNumber:     [''],
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator },
  );

  get firstName()       { return this.form.get('firstName')!; }
  get lastName()        { return this.form.get('lastName')!; }
  get dateOfBirth()     { return this.form.get('dateOfBirth')!; }
  get phoneNumber()     { return this.form.get('phoneNumber')!; }
  get email()           { return this.form.get('email')!; }
  get password()        { return this.form.get('password')!; }
  get confirmPassword() { return this.form.get('confirmPassword')!; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    const { firstName, lastName, dateOfBirth, phoneNumber, email, password } = this.form.value;

    this.authService
      .register({
        firstName:   firstName!,
        lastName:    lastName!,
        dateOfBirth: dateOfBirth || undefined,
        phoneNumber: phoneNumber || undefined,
        email:       email!,
        password:    password!,
      })
      .subscribe({
        next: (res) => {
          this.authService.saveToken(res.token);
          this.router.navigate(['/profile/me']);
        },
        error: (err) => {
          this.errorMessage = err.userMessage ?? 'Registration failed. Please try again.';
          this.loading = false;
        },
      });
  }
}
