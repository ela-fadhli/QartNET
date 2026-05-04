import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Button } from 'primeng/button';
import { ProgressBar } from 'primeng/progressbar';
import { Tag } from 'primeng/tag';
import { Avatar } from 'primeng/avatar';
import { Timeline } from 'primeng/timeline';
import { ProjectService } from '../../../../core/services/project.service';
import { Project } from '../../../../shared/models/project.model';

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
    Timeline
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.projectService.getProjectById(+id).subscribe(data => {
        this.project.set(data);
        
        // Mock data for UI demonstration if fields are empty
        if (!data.timeline || data.timeline.length === 0) {
          this.project.update(p => p ? {
            ...p,
            timeline: [
              { date: '2026-04-10', event: 'Project Initialized', icon: 'pi pi-plus', color: '#C9A84C' },
              { date: '2026-04-15', event: 'Architecture Review', icon: 'pi pi-check', color: '#C9A84C' },
              { date: '2026-05-01', event: 'Development Phase Alpha', icon: 'pi pi-cog', color: '#C9A84C' }
            ],
            team: [
              { userId: '1', name: 'Alex Rivera', role: 'Lead Architect', avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex' },
              { userId: '2', name: 'Sarah Chen', role: 'Backend Engineer', avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah' }
            ]
          } : null);
        }
      });
    }
  }

  viewRepository() {
    const project = this.project();
    if (!project?.repositoryName) return;
    const [owner, ...rest] = project.repositoryName.split('/');
    const name = rest.join('/');
    if (!owner || !name) return;
    this.router.navigate(['/repositories', owner, name]);
  }
}
