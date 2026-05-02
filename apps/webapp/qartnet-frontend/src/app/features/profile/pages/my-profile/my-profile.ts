import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProfileService } from '../../services/profile.service';
import { ProfileResponse } from '../../models/profile.models';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [ReactiveFormsModule, InputTextModule, TextareaModule, ButtonModule, MessageModule],
  templateUrl: './my-profile.html',
})
export class ProfileMyProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private profileService = inject(ProfileService);
  private cdr = inject(ChangeDetectorRef);

  profile: ProfileResponse | null = null;
  editing = false;
  loading = false;
  saving = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  form = this.fb.group({
    username:          ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    bio:               ['', Validators.maxLength(300)],
    firstName:         ['', Validators.maxLength(50)],
    lastName:          ['', Validators.maxLength(50)],
    dateOfBirth:       [''],
    phoneNumber:       [''],
  });

  get username()    { return this.form.get('username')!; }
  get bio()         { return this.form.get('bio')!; }
  get firstName()   { return this.form.get('firstName')!; }
  get lastName()    { return this.form.get('lastName')!; }
  get dateOfBirth() { return this.form.get('dateOfBirth')!; }
  get phoneNumber() { return this.form.get('phoneNumber')!; }

  ngOnInit(): void {
    this.loading = true;
    this.profileService.getMyProfile().subscribe({
      next: (profile) => {
        console.log('Profile received:', profile);
        this.profile = profile;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Profile error:', err);
        this.errorMessage = err.userMessage ?? 'Failed to load profile.';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  startEditing(): void {
    if (!this.profile) return;
    this.form.patchValue({
      username:    this.profile.username,
      bio:         this.profile.bio ?? '',
      firstName:   this.profile.firstName ?? '',
      lastName:    this.profile.lastName ?? '',
      dateOfBirth: this.profile.dateOfBirth ?? '',
      phoneNumber: this.profile.phoneNumber ?? '',
    });
    this.editing = true;
    this.successMessage = null;
    this.errorMessage = null;
  }

  cancelEditing(): void {
    this.editing = false;
    this.form.reset();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = null;

    const { username, bio, firstName, lastName, dateOfBirth, phoneNumber } = this.form.value;

    this.profileService.updateMyProfile({
      username:    username ?? undefined,
      bio:         bio ?? undefined,
      firstName:   firstName ?? undefined,
      lastName:    lastName ?? undefined,
      dateOfBirth: dateOfBirth ?? undefined,
      phoneNumber: phoneNumber ?? undefined,
    }).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.editing = false;
        this.saving = false;
        this.successMessage = 'Profile updated successfully.';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.userMessage ?? 'Failed to update profile.';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }
}
