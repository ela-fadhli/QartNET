import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe, KeyValuePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../../auth/services/auth.service';
import { UserProfile } from '../../models/profile.models';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    ReactiveFormsModule, FormsModule, RouterLink, DatePipe, KeyValuePipe,
    ButtonModule, InputTextModule, TextareaModule, TagModule,
  ],
  templateUrl: './profile-page.html',
})
export class ProfilePageComponent implements OnInit {
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  profile = signal<UserProfile | null>(null);
  loading = signal(true);
  editMode = signal(false);
  saving = signal(false);
  skillInput = '';

  form = this.fb.group({
    firstName: ['', Validators.maxLength(50)],
    lastName: ['', Validators.maxLength(50)],
    bio: ['', Validators.maxLength(1000)],
    profilePictureUrl: [''],
    skills: [[] as string[]],
  });

  addSkill(event: Event): void {
    event.preventDefault();
    const val = this.skillInput.trim();
    if (!val) return;
    const current = (this.form.value.skills as string[]) ?? [];
    if (!current.includes(val)) {
      this.form.patchValue({ skills: [...current, val] });
    }
    this.skillInput = '';
  }

  removeSkill(skill: string): void {
    const current = (this.form.value.skills as string[]) ?? [];
    this.form.patchValue({ skills: current.filter((s) => s !== skill) });
  }

  ngOnInit(): void {
    this.profileService.getMyUserProfile().subscribe({
      next: (p) => {
        this.profile.set(p);
        this.form.patchValue({
          firstName: p.firstName ?? '',
          lastName: p.lastName ?? '',
          bio: p.bio ?? '',
          profilePictureUrl: p.profilePictureUrl ?? '',
          skills: p.skills ?? [],
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  toggleEdit(): void {
    this.editMode.update((v) => !v);
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving.set(true);

    const { firstName, lastName, bio, profilePictureUrl, skills } = this.form.value;

    this.profileService.updateMyUserProfile({
      firstName: firstName || undefined,
      lastName: lastName || undefined,
      bio: bio || undefined,
      profilePictureUrl: profilePictureUrl || undefined,
      skills: (skills as string[]) || [],
    }).subscribe({
      next: (updated) => {
        this.profile.set(updated);
        this.editMode.set(false);
        this.saving.set(false);
        this.messageService.add({ severity: 'success', summary: 'Profile updated', life: 3000 });
      },
      error: () => {
        this.saving.set(false);
        this.messageService.add({ severity: 'error', summary: 'Update failed', life: 3000 });
      },
    });
  }

  logout(): void {
    this.authService.logout();
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  getInitials(p: UserProfile): string {
    const fn = p.firstName?.charAt(0) ?? '';
    const ln = p.lastName?.charAt(0) ?? '';
    return (fn + ln).toUpperCase() || p.username.charAt(0).toUpperCase();
  }

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    const map: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast'> = {
      ACTIVE: 'success', PENDING: 'warn', SUSPENDED: 'danger', DISABLED: 'secondary'
    };
    return map[status] ?? 'secondary';
  }

  getRoleSeverity(role: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    const map: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast'> = {
      ADMIN: 'danger', TEACHER: 'info', STUDENT: 'secondary'
    };
    return map[role] ?? 'secondary';
  }
}
