import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { ProgressBar } from 'primeng/progressbar';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { Dialog } from 'primeng/dialog';
import { Tag } from 'primeng/tag';
import { ProjectService } from '../../../../core/services/project.service';
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
  
  newProject = {
    title: '',
    description: '',
    repositoryName: '',
    deadline: '',
    progress: 0,
    teamCount: 1
  };

  constructor(private projectService: ProjectService) {}

  ngOnInit() {
    this.loadProjects();
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
    this.projectService.createProject(this.newProject as Project).subscribe(() => {
      this.loadProjects();
      this.showCreateDialog.set(false);
      this.resetNewProject();
    });
  }

  resetNewProject() {
    this.newProject = {
      title: '',
      description: '',
      repositoryName: '',
      deadline: '',
      progress: 0,
      teamCount: 1
    };
  }
}
