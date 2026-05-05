import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { ProgressBar } from 'primeng/progressbar';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { Dialog } from 'primeng/dialog';
import { Tag } from 'primeng/tag';
import { ProjectService } from '../../../../core/services/project.service';
import { ProfileService } from '../../../profile/services/profile.service';
import { Project } from '../../../../shared/models/project.model';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    Button,
    Card,
    ProgressBar,
    InputText,
    Textarea,
    Tag,
    Dialog
  ],
  templateUrl: './project-list.html',
  styles: [`
    :host ::ng-deep .p-card {
      background: #0D1221;
      border: 1px solid rgba(201, 168, 76, 0.1);
      transition: transform 0.2s, border-color 0.2s;
    }
    :host ::ng-deep .p-card:hover {
      transform: translateY(-4px);
      border-color: rgba(201, 168, 76, 0.3);
    }
    :host ::ng-deep .p-progressbar {
      background: rgba(232, 213, 163, 0.05);
      height: 6px;
    }
    :host ::ng-deep .p-progressbar-value {
      background: #C9A84C;
    }
  `]
})
export class ProjectListComponent implements OnInit {
  projects = signal<Project[]>([]);
  showCreateDialog = signal(false);
  currentUserName = signal<string>('Project Lead');
  currentUserUsername = signal<string>('');
  
  newProject: Project = {
    title: '',
    description: '',
    repositoryName: '',
    deadline: '',
    progress: 0,
    teamCount: 1,
    phases: []
  };

  constructor(
    private projectService: ProjectService,
    private profileService: ProfileService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadProjects();
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

  loadProjects() {
    this.projectService.getAllProjects().subscribe(data => {
      this.projects.set(data);
    });
  }

  openCreateDialog() {
    this.showCreateDialog.set(true);
  }

  createProject() {
    const creatorName = this.currentUserName();
    const creatorUsername = this.currentUserUsername();
    this.newProject.creatorName = creatorName;
    this.newProject.creatorUsername = creatorUsername;
    
    // Explicitly add the Lead member in the frontend
    this.newProject.team = [{
      name: creatorName,
      role: 'Lead Engineer',
      specialty: 'System Architecture',
      userId: 'admin', // Placeholder
      avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + creatorName
    }];

    this.projectService.createProject(this.newProject as Project).subscribe(project => {
      this.loadProjects();
      this.showCreateDialog.set(false);
      this.resetNewProject();
      if (project && project.id) {
        this.router.navigate(['/projects', project.id]);
      }
    });
  }

  deleteProject(event: Event, id: number) {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this project?')) {
      this.projectService.deleteProject(id).subscribe(() => {
        this.loadProjects();
      });
    }
  }

  addPhase() {
    this.newProject.phases?.push({
      title: '',
      status: 'pending',
      tasks: []
    });
  }

  removePhase(index: number) {
    this.newProject.phases?.splice(index, 1);
  }

  addTask(phaseIndex: number) {
    this.newProject.phases?.[phaseIndex].tasks?.push({
      title: '',
      status: 'pending',
      assigneeInitials: 'AK',
      dueDate: ''
    } as any);
  }

  removeTask(phaseIndex: number, taskIndex: number) {
    this.newProject.phases?.[phaseIndex].tasks?.splice(taskIndex, 1);
  }

  resetNewProject() {
    this.newProject = {
      title: '',
      description: '',
      repositoryName: '',
      deadline: '',
      progress: 0,
      teamCount: 1,
      phases: []
    };
  }

  viewFunctionalControl(event: Event, project: Project) {
    event.stopPropagation();
    if (!project.repositoryName) return;
    const [owner, ...rest] = project.repositoryName.split('/');
    const name = rest.join('/');
    if (!owner || !name) return;
    this.router.navigate(['/repositories', owner, name]);
  }
}
