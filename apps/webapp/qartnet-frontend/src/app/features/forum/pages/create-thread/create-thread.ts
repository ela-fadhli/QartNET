import { ChangeDetectorRef, Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { Select } from 'primeng/select';
import { ForumService } from '../../services/forum.service';
import { ForumCategoryResponse } from '../../models/forum.models';

@Component({
  selector: 'app-create-thread',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, ButtonModule, InputTextModule, TextareaModule, MessageModule, Select],
  templateUrl: './create-thread.html',
})
export class CreateThreadComponent implements OnInit {
  private forumService = inject(ForumService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  slug = '';
  categories = signal<ForumCategoryResponse[]>([]);
  tagNames = signal<string[]>([]);
  submitting = signal(false);
  error = signal<string | null>(null);

  categoryOptions = computed(() =>
    this.categories().map((c) => ({ label: c.name, value: c.publicId })),
  );

  form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
    body: ['', [Validators.required, Validators.minLength(10)]],
    categoryPublicId: [null as string | null, Validators.required],
  });

  get title() { return this.form.get('title')!; }
  get body() { return this.form.get('body')!; }
  get categoryPublicId() { return this.form.get('categoryPublicId')!; }

  ngOnInit(): void {
    this.slug = this.route.snapshot.paramMap.get('slug')!;
    this.forumService.getCategories(this.slug).subscribe({
      next: (cats) => {
        this.categories.set(cats);
        this.cdr.markForCheck();
      },
    });
  }

  onTagKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      const input = event.target as HTMLInputElement;
      const val = input.value.trim().toLowerCase().replace(/[^a-z0-9-]/g, '');
      if (val && !this.tagNames().includes(val) && this.tagNames().length < 10) {
        this.tagNames.update((t) => [...t, val]);
      }
      input.value = '';
    }
  }

  removeTag(tag: string): void {
    this.tagNames.update((t) => t.filter((n) => n !== tag));
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { title, body, categoryPublicId } = this.form.value;

    this.forumService
      .createThread(this.slug, {
        title: title!,
        body: body!,
        categoryPublicId: categoryPublicId!,
        tagNames: this.tagNames(),
      })
      .subscribe({
        next: (thread) => {
          this.router.navigate(['/forum/threads', thread.publicId]);
        },
        error: (err) => {
          this.error.set(err.userMessage ?? 'Failed to create thread.');
          this.submitting.set(false);
          this.cdr.markForCheck();
        },
      });
  }
}
