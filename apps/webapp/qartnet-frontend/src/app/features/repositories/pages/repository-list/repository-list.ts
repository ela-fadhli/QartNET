import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { Tag } from 'primeng/tag';
import { Dialog } from 'primeng/dialog';
import { SelectButton } from 'primeng/selectbutton';
import { Avatar } from 'primeng/avatar';
import { RepositoryService } from '../../../../core/services/repository.service';
import { Repository } from '../../../../shared/models/repository.model';

@Component({
  selector: 'app-repository-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    Button,
    InputText,
    Textarea,
    Tag,
    Dialog,
    SelectButton,
    Avatar
  ],
  templateUrl: './repository-list.html',
  styles: [`
    .repo-item {
      background: #0D1221;
      border: 1px solid rgba(201, 168, 76, 0.05);
      border-radius: 12px;
      padding: 1.5rem;
      transition: all 0.2s;
    }
    .repo-item:hover {
      border-color: rgba(201, 168, 76, 0.25);
      background: rgba(201, 168, 76, 0.02);
      transform: translateX(4px);
    }
  `]
})
export class RepositoryListComponent implements OnInit {
  repositories = signal<Repository[]>([]);
  showCreateDialog = signal(false);
  searchQuery = signal('');
  selectedFilter = signal<'ALL' | 'PUBLIC' | 'PRIVATE'>('ALL');

  filteredRepositories = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const filter = this.selectedFilter();
    
    return this.repositories().filter(repo => {
      const matchesSearch = repo.name.toLowerCase().includes(query) || 
                           (repo.description?.toLowerCase().includes(query) || false) ||
                           repo.owner.toLowerCase().includes(query);
      
      const matchesFilter = filter === 'ALL' || repo.visibility === filter;
      
      return matchesSearch && matchesFilter;
    });
  });
  
  visibilityOptions = [
    { label: 'Public', value: 'PUBLIC' },
    { label: 'Private', value: 'PRIVATE' }
  ];

  newRepo: Repository = {
    owner: 'ela-fadhli', // Default value
    ownerDisplayName: 'Ela Fadhli',
    name: '',
    description: '',
    visibility: 'PUBLIC'
  };

  constructor(private repositoryService: RepositoryService) {}

  ngOnInit() {
    this.loadRepositories();
  }

  loadRepositories() {
    this.repositoryService.getAllRepositories().subscribe(data => {
      this.repositories.set(data);
    });
  }

  openCreateDialog() {
    this.showCreateDialog.set(true);
  }

  createRepository() {
    this.repositoryService.createRepository(this.newRepo).subscribe(() => {
      this.loadRepositories();
      this.showCreateDialog.set(false);
      this.resetNewRepo();
    });
  }

  resetNewRepo() {
    this.newRepo = {
      owner: 'ela-fadhli',
      ownerDisplayName: 'Ela Fadhli',
      name: '',
      description: '',
      visibility: 'PUBLIC'
    };
  }

  getLanguageColor(lang: string | undefined): string {
    const colors: Record<string, string> = {
      'Java': '#b07219',
      'TypeScript': '#3178c6',
      'JavaScript': '#f1e05a',
      'HTML': '#e34c26',
      'CSS': '#563d7c',
      'Python': '#3572A5'
    };
    return colors[lang || ''] || '#8b949e';
  }
}
