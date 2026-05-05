import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Project } from '../../shared/models/project.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = `${environment.apiUrl}/api/projects`;

  constructor(private http: HttpClient) {}

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  createProject(project: Project): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, project);
  }

  linkRepository(projectId: number, repositoryId: number): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${projectId}/repository/${repositoryId}`, {});
  }

  unlinkRepository(projectId: number): Observable<Project> {
    return this.http.delete<Project>(`${this.apiUrl}/${projectId}/repository`);
  }

  updateTaskStatus(taskId: number, status: string): Observable<Project> {
    return this.http.patch<Project>(`${this.apiUrl}/tasks/${taskId}/status?status=${status}`, {});
  }

  recalculateProgress(projectId: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${projectId}/recalculate-progress`);
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addMember(projectId: number, member: any): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/${projectId}/members`, member);
  }

  addPhase(projectId: number, phase: any): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/${projectId}/phases`, phase);
  }

  addTask(phaseId: number, task: any): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/phases/${phaseId}/tasks`, task);
  }
}
