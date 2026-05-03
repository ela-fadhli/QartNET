import { ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { ForumService } from '../../services/forum.service';

@Component({
  selector: 'app-create-forum',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, ButtonModule, InputTextModule, TextareaModule, MessageModule],
  templateUrl: './create-forum.html',
})
export class CreateForumComponent {
  private forumService = inject(ForumService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  submitting = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    slug: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]{3,100}$/)]],
    description: ['', Validators.maxLength(500)],
  });

  get name() { return this.form.get('name')!; }
  get slug() { return this.form.get('slug')!; }
  get description() { return this.form.get('description')!; }

  onNameInput(): void {
    const raw = this.name.value ?? '';
    const slug = raw
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
    this.form.patchValue({ slug }, { emitEvent: false });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { name, slug, description } = this.form.value;

    this.forumService
      .createForum({
        name: name!,
        slug: slug!,
        description: description || null,
        banner: null,
      })
      .subscribe({
        next: (forum) => {
          this.router.navigate(['/forum', forum.slug]);
        },
        error: (err) => {
          this.error.set(err.userMessage ?? 'Failed to create forum.');
          this.submitting.set(false);
          this.cdr.markForCheck();
        },
      });
  }
}
