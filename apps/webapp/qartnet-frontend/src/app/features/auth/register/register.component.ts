import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';


import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    SelectModule
  ],
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  roles: any[] = [];
  departments: any[] = [];

  registerForm: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email, Validators.pattern(/^[a-zA-Z0-9._%+-]+@enicar\.ucar\.tn$/)]],
    role: [null, Validators.required],
    department: [null],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  isLoading = false;
  successMessage = '';
  errorMessage = '';

  ngOnInit() {
    this.roles = [
      { name: 'Student', code: 'STUDENT' },
      { name: 'Professor', code: 'PROFESSOR' }
    ];

    this.departments = [
      { name: 'Computer Science', code: 'CS' },
      { name: 'Mechatronics', code: 'MECH' },
      { name: 'Infotronics', code: 'INFO' },
      { name: 'Industrial Systems', code: 'INDST' }
    ];
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = 'Account created successfully! Please check your email to verify your account.';
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'An error occurred during registration. Please try again.';
      }
    });
  }
}
