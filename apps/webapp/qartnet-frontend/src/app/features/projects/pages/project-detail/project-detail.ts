import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { ProgressBar } from 'primeng/progressbar';
import { Tag } from 'primeng/tag';
import { Avatar } from 'primeng/avatar';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { ProjectService } from '../../../../core/services/project.service';
import { RepositoryService } from '../../../../core/services/repository.service';
import { ProfileService } from '../../../profile/services/profile.service';
import { Project, ProjectPhase, ProjectTask } from '../../../../shared/models/project.model';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    Button,
    ProgressBar,
    Tag,
    Avatar,
    Dialog,
    InputText,
    FormsModule,
    TooltipModule
  ],
  templateUrl: './project-detail.html',
  styles: [`
    .detail-card {
      background: #0D1221;
      border: 1px solid rgba(201, 168, 76, 0.1);
      border-radius: 16px;
      padding: 2rem;
    }
    :host ::ng-deep .p-timeline-event-content {
      color: #E8D5A3/70;
      font-size: 0.85rem;
    }
    :host ::ng-deep .p-timeline-event-opposite {
      color: #E8D5A3/30;
      font-size: 0.75rem;
    }
  `]
})
export class ProjectDetailComponent implements OnInit {
  project = signal<Project | null>(null);
  recentCommits = signal<any[]>([]);
  activeTab = signal('phases');
  showAddMemberDialog = signal(false);
  showAddPhaseDialog = signal(false);
  showAddTaskDialog = signal(false);
  currentUserName = signal<string | null>(null);
  currentUserUsername = signal<string | null>(null);
  selectedPhaseId = signal<number | null>(null);

  sortedPhases = computed(() => {
    const p = this.project();
    if (!p || !p.phases) return [];
    
    const statusOrder: { [key: string]: number } = {
      'COMPLETED': 1,
      'IN_PROGRESS': 2,
      'PENDING': 3
    };

    return [...p.phases].sort((a, b) => {
      const orderA = statusOrder[a.status || 'PENDING'] || 99;
      const orderB = statusOrder[b.status || 'PENDING'] || 99;
      return orderA - orderB;
    });
  });
  
  newMember = {
    name: '',
    role: '',
    userId: '',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + Math.random(),
    specialty: ''
  };

  newPhase = {
    title: '',
    status: 'pending'
  };

  newTask = {
    title: '',
    status: 'pending',
    assigneeInitials: 'AK'
  };

  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private repositoryService: RepositoryService,
    private profileService: ProfileService,
    private router: Router
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProject(+id);
    }
    this.loadUserProfile();
  }

  loadUserProfile() {
    this.profileService.getMyProfile().subscribe(profile => {
      const name = profile.firstName && profile.lastName 
        ? `${profile.firstName} ${profile.lastName}`
        : profile.username;
      this.currentUserName.set(name);
      this.currentUserUsername.set(profile.username);
    });
  }

  // Reactive computed signal — strictly checks if logged-in user is the project creator
  isCreator = computed(() => {
    const p = this.project();
    const currentUsername = this.currentUserUsername();
    const currentName = this.currentUserName();

    if (!p || !currentUsername) return false;

    // 1. Strict match on unique username
    if (p.creatorUsername && p.creatorUsername === currentUsername) return true;

    // 2. Fallback on display name for legacy projects (no creatorUsername set)
    if (!p.creatorUsername && currentName && p.creatorName === currentName) return true;

    return false;
  });

  loadProject(id: number) {
    this.projectService.getProjectById(id).subscribe(data => {
      this.project.set(data);
      if (data.repositoryName) {
        this.loadGitActivity(data.repositoryName);
      }
      
      // Retroactive personalization for old projects
      if (data.team?.some(m => m.name === 'Project Lead')) {
        this.profileService.getMyProfile().subscribe(profile => {
          const name = profile.firstName && profile.lastName 
            ? `${profile.firstName} ${profile.lastName}`
            : profile.username;
          
          // Update the lead member's name in the UI immediately
          const lead = data.team?.find(m => m.name === 'Project Lead');
          if (lead) {
            lead.name = name;
            // Optionally, we could call an API to save this change permanently
            // For now, updating the UI is enough to "wow" the user
          }
        });
      }
    });
  }

  getPhaseProgress(phase: ProjectPhase): number {
    if (!phase.tasks || phase.tasks.length === 0) return 0;
    const completed = phase.tasks.filter((t: ProjectTask) => t.status === 'completed').length;
    return Math.round((completed * 100) / phase.tasks.length);
  }

  loadGitActivity(repoFullName: string) {
    const [owner, ...rest] = repoFullName.split('/');
    const name = rest.join('/');
    this.repositoryService.getCommits(owner, name).subscribe(commits => {
      this.recentCommits.set(commits.slice(0, 3));
    });
  }

  switchTab(tab: string) {
    this.activeTab.set(tab);
  }

  addMember() {
    const p = this.project();
    if (!p?.id) return;
    this.projectService.addMember(p.id, this.newMember).subscribe(updatedProject => {
      this.project.set(updatedProject);
      this.showAddMemberDialog.set(false);
      this.newMember = {
        name: '',
        role: '',
        userId: '',
        avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + Math.random(),
        specialty: ''
      };
    });
  }

  addPhase() {
    const p = this.project();
    if (!p?.id) return;
    this.projectService.addPhase(p.id, this.newPhase).subscribe(updatedProject => {
      this.project.set(updatedProject);
      this.showAddPhaseDialog.set(false);
      this.newPhase = { title: '', status: 'pending' };
    });
  }

  openAddTaskDialog(phaseId: number) {
    this.selectedPhaseId.set(phaseId);
    this.showAddTaskDialog.set(true);
  }

  addTask() {
    const phaseId = this.selectedPhaseId();
    if (!phaseId) return;
    this.projectService.addTask(phaseId, this.newTask).subscribe(updatedProject => {
      this.project.set(updatedProject);
      this.showAddTaskDialog.set(false);
      this.newTask = { title: '', status: 'pending', assigneeInitials: 'AK' };
    });
  }

  viewRepository() {
    const project = this.project();
    if (!project?.repositoryName) return;
    const [owner, ...rest] = project.repositoryName.split('/');
    const name = rest.join('/');
    if (!owner || !name) return;
    this.router.navigate(['/repositories', owner, name]);
  }

  toggleTaskStatus(taskId: number, currentStatus: string) {
    const newStatus = currentStatus === 'completed' ? 'pending' : 'completed';
    this.projectService.updateTaskStatus(taskId, newStatus).subscribe(updatedProject => {
      this.project.set(updatedProject);
    });
  }
}
