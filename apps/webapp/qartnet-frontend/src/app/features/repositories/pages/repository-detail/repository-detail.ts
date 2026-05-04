import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Tabs } from 'primeng/tabs';
import { TabList } from 'primeng/tabs';
import { Tab } from 'primeng/tabs';
import { TabPanels } from 'primeng/tabs';
import { TabPanel } from 'primeng/tabs';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Avatar } from 'primeng/avatar';
import { Popover } from 'primeng/popover';
import { InputText } from 'primeng/inputtext';
import { RepositoryService } from '../../../../core/services/repository.service';
import { Repository, RepositoryCommit, RepositoryFile } from '../../../../shared/models/repository.model';

@Component({
  selector: 'app-repository-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    Button,
    Tabs,
    TabList,
    Tab,
    TabPanels,
    TabPanel,
    TableModule,
    Tag,
    Avatar,
    Popover,
    InputText
  ],
  templateUrl: './repository-detail.html',
  styles: [`
    :host ::ng-deep .p-tabs {
      background: transparent;
    }
    :host ::ng-deep .p-tablist-tab {
      background: transparent !important;
      border-color: transparent !important;
      color: #E8D5A3/50 !important;
      font-size: 0.85rem !important;
      padding: 1rem 1.5rem !important;
    }
    :host ::ng-deep .p-tablist-tab.p-tab-active {
      color: #C9A84C !important;
      border-bottom-color: #C9A84C !important;
    }
    :host ::ng-deep .p-datatable {
      background: #0D1221;
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid rgba(201, 168, 76, 0.05);
    }
    :host ::ng-deep .p-datatable .p-datatable-thead > tr > th {
      background: rgba(201, 168, 76, 0.02);
      color: #E8D5A3/40;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      border-bottom: 1px solid rgba(201, 168, 76, 0.05);
      padding: 1rem;
    }
    :host ::ng-deep .p-datatable .p-datatable-tbody > tr {
      background: transparent;
      color: #E8D5A3/80;
      transition: background 0.2s;
    }
    :host ::ng-deep .p-datatable .p-datatable-tbody > tr:hover {
      background: rgba(201, 168, 76, 0.03) !important;
    }
    :host ::ng-deep .p-datatable .p-datatable-tbody > tr > td {
      border-bottom: 1px solid rgba(201, 168, 76, 0.03);
      padding: 0.75rem 1rem;
      font-size: 0.85rem;
    }
  `]
})
export class RepositoryDetailComponent implements OnInit {
  repository = signal<Repository | null>(null);
  files = signal<RepositoryFile[]>([]);
  commits = signal<RepositoryCommit[]>([]);
  branches = signal<string[]>([]);
  activeBranch = signal('main');
  stats = signal<any>(null);
  renameValue = '';
  mergeSource = '';
  mergeTarget = 'main';
  commitDetails = signal<RepositoryCommit | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private repositoryService: RepositoryService
  ) {}

  ngOnInit() {
    const owner = this.route.snapshot.paramMap.get('owner');
    const name = this.route.snapshot.paramMap.get('name');

    if (owner && name) {
      this.repositoryService.getByOwnerAndName(owner, name).subscribe(repo => {
        this.repository.set(repo);
        this.renameValue = repo.name;
        this.loadGitData(owner, name);
      });
    }
  }

  loadGitData(owner: string, name: string) {
    const branch = this.activeBranch();

    this.repositoryService.getBranches(owner, name).subscribe(branches => {
      this.branches.set(branches);
      if (branches.length > 0 && !branches.includes(branch)) {
        this.activeBranch.set(branches[0]);
      }
    });

    this.repositoryService.getFiles(owner, name, branch).subscribe(files => {
      this.files.set(files);
      const readme = files.find(f => f.name.toLowerCase() === 'readme.md');
      if (readme) {
        this.repositoryService.getFileContent(owner, name, readme.name, branch).subscribe(res => {
          if (this.repository()) {
             const repo = this.repository()!;
             repo.readmeSubtitle = res.content; // Use subtitle field to store readme content for simplicity in display
          }
        });
      }
    });

    this.repositoryService.getCommits(owner, name, this.activeBranch()).subscribe(commits => {
      this.commits.set(commits);
      // Update stats based on real data
      this.stats.set({
        commits: commits.length,
        contributors: new Set(commits.map(c => c.author)).size,
        stars: this.repository()?.stars || 0,
        forks: this.repository()?.forks || 0
      });
    });
  }

  switchBranch(branch: string) {
    this.activeBranch.set(branch);
    const repo = this.repository();
    if (repo) {
      this.loadGitData(repo.owner, repo.name);
    }
  }

  deleteRepository() {
    const repo = this.repository();
    console.log('Attempting to delete repository:', repo);
    
    if (!repo || !repo.id) {
      console.error('Cannot delete: Repository ID is missing', repo);
      return;
    }

    if (confirm(`Are you sure you want to delete "${repo.name}"? This action cannot be undone.`)) {
      this.repositoryService.deleteRepository(repo.id).subscribe({
        next: () => {
          console.log('Repository deleted successfully');
          this.router.navigate(['/repositories']);
        },
        error: (err) => {
          console.error('Failed to delete repository', err);
        }
      });
    }
  }

  toggleStar() {
    const repo = this.repository();
    if (repo) {
      this.repositoryService.star(repo.owner, repo.name).subscribe(updated => {
        this.repository.set(updated);
      });
    }
  }

  fork() {
    const repo = this.repository();
    if (repo) {
      const newOwner = 'current-user'; // Replace with actual current user
      this.repositoryService.fork(repo.owner, repo.name, newOwner).subscribe(forked => {
        this.router.navigate(['/repositories', forked.owner, forked.name]);
      });
    }
  }

  download() {
    const repo = this.repository();
    if (repo) {
      const url = `http://localhost:8085/api/repositories/${repo.owner}/${repo.name}/download?branch=${this.activeBranch()}`;
      window.location.href = url;
    }
  }

  renameRepository() {
    const repo = this.repository();
    if (!repo || !this.renameValue || this.renameValue === repo.name) return;
    this.repositoryService.updateRepository(repo.owner, repo.name, { name: this.renameValue }).subscribe(updated => {
      this.repository.set(updated);
      this.router.navigate(['/repositories', updated.owner, updated.name]);
    });
  }

  changeVisibility() {
    const repo = this.repository();
    if (!repo) return;
    const visibility = repo.visibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC';
    this.repositoryService.updateRepository(repo.owner, repo.name, { visibility }).subscribe(updated => {
      this.repository.set(updated);
    });
  }

  createBranch() {
    const repo = this.repository();
    if (!repo) return;
    const branchName = prompt('New branch name');
    if (!branchName) return;
    this.repositoryService.createBranch(repo.owner, repo.name, branchName, this.activeBranch()).subscribe(() => {
      this.loadGitData(repo.owner, repo.name);
    });
  }

  mergeBranch() {
    const repo = this.repository();
    if (!repo || !this.mergeSource || !this.mergeTarget) return;
    this.repositoryService.mergeBranch(repo.owner, repo.name, this.mergeSource, this.mergeTarget).subscribe(() => {
      this.loadGitData(repo.owner, repo.name);
      this.mergeSource = '';
    });
  }

  showCommitDetails(hash: string) {
    const repo = this.repository();
    if (!repo) return;
    this.repositoryService.getCommitDetails(repo.owner, repo.name, hash).subscribe(details => {
      this.commitDetails.set(details);
    });
  }
}
