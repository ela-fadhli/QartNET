import { Component, OnInit, signal, computed } from '@angular/core';
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
import { Dialog } from 'primeng/dialog';
import { RepositoryService } from '../../../../core/services/repository.service';
import { ProfileService } from '../../../profile/services/profile.service';
import { Repository, RepositoryAccess, RepositoryCommit, RepositoryFile } from '../../../../shared/models/repository.model';

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
    InputText,
    Dialog
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
  currentPath = signal('');
  renameValue = '';
  newBranchName = signal('');
  mergeSource = '';
  mergeTarget = 'main';
  commitDetails = signal<RepositoryCommit | null>(null);
  selectedFileContent = signal<string | null>(null);
  selectedFileName = signal<string>('');
  showFileViewer = signal(false);
  accessList = signal<RepositoryAccess[]>([]);
  newAccess = { actorKey: '', role: 'COLLABORATOR' as any, level: 'READ' as any };
  currentUserUsername = signal<string | null>(null);
  currentUserDisplayName = signal<string | null>(null);

  isOwner = computed(() => {
    const repo = this.repository();
    const currentUsername = this.currentUserUsername()?.toLowerCase();
    const currentDisplayName = this.currentUserDisplayName()?.toLowerCase();
    
    if (!repo || (!currentUsername && !currentDisplayName)) return false;
    
    const repoOwner = repo.owner.toLowerCase();
    
    // Check all possible identity matches
    const possibleIdentities = new Set<string>();
    if (currentUsername) {
      possibleIdentities.add(currentUsername);
      possibleIdentities.add(currentUsername.replace(/\s+/g, '-'));
    }
    if (currentDisplayName) {
      possibleIdentities.add(currentDisplayName);
      possibleIdentities.add(currentDisplayName.replace(/\s+/g, '-'));
    }

    const isMatch = possibleIdentities.has(repoOwner);
    
    console.log('--- Ownership Check ---', {
      repoOwner,
      possibleIdentities: Array.from(possibleIdentities),
      isMatch
    });
    
    return isMatch;
  });

  stats = computed(() => {
    const commits = this.commits();
    const repo = this.repository();

    return {
      commits: commits.length,
      contributors: new Set(commits.map(c => c.author)).size,
      stars: repo?.stars || 0,
      forks: repo?.forks || 0,
      watchers: repo?.watchers || 0
    };
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private repositoryService: RepositoryService,
    private profileService: ProfileService
  ) {}

  ngOnInit() {
    this.profileService.getMyProfile().subscribe(profile => {
      this.currentUserUsername.set(profile.username);
      if (profile.firstName && profile.lastName) {
        this.currentUserDisplayName.set(`${profile.firstName} ${profile.lastName}`);
      }
    });
    const owner = this.route.snapshot.paramMap.get('owner');
    const name = this.route.snapshot.paramMap.get('name');

    if (owner && name) {
      this.repositoryService.getByOwnerAndName(owner, name).subscribe(repo => {
        this.repository.set(repo);
        this.renameValue = repo.name;
        this.activeBranch.set(repo.defaultBranch || 'main');
        this.loadGitData(owner, name);
        this.loadAccessData(owner, name);
      });
    }
  }

  loadGitData(owner: string, name: string) {
    this.repositoryService.getBranches(owner, name).subscribe(branches => {
      this.branches.set(branches);
      
      // Select best branch: defaultBranch -> current active -> first available -> 'main'
      const repo = this.repository();
      let targetBranch = repo?.defaultBranch || this.activeBranch();
      
      if (branches.length > 0 && !branches.includes(targetBranch)) {
        targetBranch = branches.includes('main') ? 'main' : (branches.includes('master') ? 'master' : branches[0]);
      }
      
      this.activeBranch.set(targetBranch);
      this.refreshBranchData(owner, name, targetBranch);
    });
  }

  refreshBranchData(owner: string, name: string, branch: string) {
    this.repositoryService.getFiles(owner, name, branch, this.currentPath()).subscribe(files => {
      this.files.set(files);
      const readme = files.find(f => f.name.toLowerCase() === 'readme.md');
      if (readme && !this.currentPath()) {
        this.repositoryService.getFileContent(owner, name, readme.name, branch).subscribe(res => {
          if (this.repository()) {
             this.repository()!.readmeSubtitle = res.content;
          }
        });
      }
    });

    this.repositoryService.getCommits(owner, name, branch).subscribe(commits => {
      this.commits.set(commits);
    });
  }

  loadAccessData(owner: string, name: string) {
    this.repositoryService.listAccess(owner, name).subscribe(access => {
      this.accessList.set(access);
    });
  }

  switchBranch(branch: string) {
    this.activeBranch.set(branch);
    const repo = this.repository();
    if (repo) {
      this.refreshBranchData(repo.owner, repo.name, branch);
    }
  }

  deleteRepository() {
    const repo = this.repository();
    if (!repo) return;
    
    console.log('>>> Action: deleteRepository triggered', repo);
    
    if (!repo.id) {
      console.error('Cannot delete: Repository ID is missing in frontend model', repo);
      alert('Error: Repository ID is missing. Please refresh the page.');
      return;
    }

    if (confirm(`Are you sure you want to delete "${repo.name}"? This action cannot be undone.`)) {
      this.repositoryService.deleteRepository(repo.id).subscribe({
        next: () => {
          console.log('>>> Repository deleted successfully');
          this.router.navigate(['/repositories']);
        },
        error: (err) => {
          console.error('>>> Failed to delete repository', err);
          alert('Failed to delete repository. Please ensure you have owner permissions.');
        }
      });
    }
  }

  toggleStar() {
    const repo = this.repository();
    if (repo) {
      // Logic for toggle star (assuming we can track if user starred, for now just star/unstar alternating or just star)
      this.repositoryService.star(repo.owner, repo.name).subscribe(updated => {
        this.repository.set(updated);
      });
    }
  }

  download() {
    const repo = this.repository();
    if (repo) {
      const url = `http://localhost:8085/api/repositories/${encodeURIComponent(repo.owner)}/${encodeURIComponent(repo.name)}/download?branch=${encodeURIComponent(this.activeBranch())}`;
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
    
    const newVisibility = repo.visibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC';
    console.log(`>>> Action: changeVisibility from ${repo.visibility} to ${newVisibility}`);
    
    this.repositoryService.updateRepository(repo.owner, repo.name, { visibility: newVisibility }).subscribe({
      next: (updated) => {
        console.log('>>> Visibility updated successfully:', updated.visibility);
        this.repository.set(updated);
      },
      error: (err) => {
        console.error('>>> Failed to change visibility', err);
        alert('Failed to change visibility. Only the repository owner can perform this action.');
      }
    });
  }

  createBranch() {
    const repo = this.repository();
    const branchName = this.newBranchName();
    if (!repo || !branchName) return;
    
    this.repositoryService.createBranch(repo.owner, repo.name, branchName, this.activeBranch()).subscribe(() => {
      this.loadGitData(repo.owner, repo.name);
      this.newBranchName.set(''); // Reset input
    });
  }

  deleteBranch(branchName: string) {
    const repo = this.repository();
    if (!repo || branchName === 'main' || branchName === repo.defaultBranch) return;
    if (confirm(`Delete branch ${branchName}?`)) {
      this.repositoryService.deleteBranch(repo.owner, repo.name, branchName).subscribe(() => {
        this.loadGitData(repo.owner, repo.name);
      });
    }
  }

  setDefaultBranch(branchName: string) {
    const repo = this.repository();
    if (!repo) return;
    this.repositoryService.setDefaultBranch(repo.owner, repo.name, branchName).subscribe(updated => {
      this.repository.set(updated);
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

  viewFile(name: string) {
    const repo = this.repository();
    if (!repo) return;
    const path = this.currentPath() ? `${this.currentPath()}/${name}` : name;
    this.repositoryService.getFileContent(repo.owner, repo.name, path, this.activeBranch()).subscribe(res => {
      this.selectedFileName.set(name);
      this.selectedFileContent.set(res.content);
      this.showFileViewer.set(true);
    });
  }

  navigateTo(name: string) {
    const repo = this.repository();
    if (!repo) return;
    const newPath = this.currentPath() ? `${this.currentPath()}/${name}` : name;
    this.currentPath.set(newPath);
    this.refreshBranchData(repo.owner, repo.name, this.activeBranch());
  }

  goBack() {
    const repo = this.repository();
    if (!repo) return;
    const parts = this.currentPath().split('/');
    parts.pop();
    this.currentPath.set(parts.join('/'));
    this.refreshBranchData(repo.owner, repo.name, this.activeBranch());
  }

  get pathSegments() {
    return this.currentPath() ? this.currentPath().split('/') : [];
  }

  addCollaborator() {
    const repo = this.repository();
    if (!repo || !this.newAccess.actorKey) return;
    this.repositoryService.upsertAccess(repo.owner, repo.name, this.newAccess as any).subscribe(() => {
      this.loadAccessData(repo.owner, repo.name);
      this.newAccess.actorKey = '';
    });
  }

  removeCollaborator(actorKey: string) {
    const repo = this.repository();
    if (!repo) return;
    
    if (confirm(`Are you sure you want to revoke access for ${actorKey}?`)) {
      console.log('Revoking access for:', actorKey);
      this.repositoryService.revokeAccess(repo.owner, repo.name, actorKey).subscribe({
        next: () => {
          console.log('Access revoked successfully');
          this.loadAccessData(repo.owner, repo.name);
        },
        error: (err) => {
          console.error('Failed to revoke access', err);
        }
      });
    }
  }
}
