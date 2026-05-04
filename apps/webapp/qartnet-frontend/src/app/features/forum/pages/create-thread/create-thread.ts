import { ChangeDetectorRef, Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { Select } from 'primeng/select';
import { MultiSelect } from 'primeng/multiselect';
import { ForumService } from '../../services/forum.service';
import { CategoryResponse, TagResponse } from '../../models/forum.models';

@Component({
  selector: 'app-create-thread',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, ButtonModule, InputTextModule, TextareaModule, MessageModule, Select, MultiSelect],
  templateUrl: './create-thread.html',
})
export class CreateThreadComponent implements OnInit {
  private forumService = inject(ForumService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  categories = signal<CategoryResponse[]>([]);
  tags = signal<TagResponse[]>([]);
  submitting = signal(false);
  error = signal<string | null>(null);

  categoryOptions = computed(() =>
    this.categories().map((c) => ({ label: c.name, value: c.publicId })),
  );

  tagOptions = computed(() =>
    this.tags().map((t) => ({ label: t.name, value: t.publicId })),
  );

  form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
    body: ['', [Validators.required, Validators.minLength(10)]],
    categoryPublicId: [null as string | null, Validators.required],
    tagPublicIds: [[] as string[]],
  });

  get title() { return this.form.get('title')!; }
  get body() { return this.form.get('body')!; }
  get categoryPublicId() { return this.form.get('categoryPublicId')!; }

  ngOnInit(): void {
    this.forumService.getCategories().subscribe((cats) => {
      this.categories.set(cats);
      this.cdr.markForCheck();
    });
    this.forumService.getTags().subscribe((tags) => {
      this.tags.set(tags);
      this.cdr.markForCheck();
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { title, body, categoryPublicId, tagPublicIds } = this.form.value;

    this.forumService
      .createThread({
        title: title!,
        body: body!,
        categoryPublicId: categoryPublicId!,
        tagPublicIds: tagPublicIds ?? [],
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
