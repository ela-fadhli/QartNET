import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Repository, RepositoryAccess, RepositoryCommit, RepositoryFile } from '../../shared/models/repository.model';

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  private apiUrl = `${environment.apiUrl}/api/repositories`;

  constructor(private http: HttpClient) {}

  getAllRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(this.apiUrl);
  }

  getByOwnerAndName(owner: string, name: string): Observable<Repository> {
    return this.http.get<Repository>(`${this.apiUrl}/${owner}/${name}`);
  }

  createRepository(repository: Repository): Observable<Repository> {
    return this.http.post<Repository>(this.apiUrl, repository);
  }

  deleteRepository(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getBranches(owner: string, name: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/${owner}/${name}/branches`);
  }

  getFiles(owner: string, name: string, branch: string = 'main'): Observable<RepositoryFile[]> {
    return this.http.get<RepositoryFile[]>(`${this.apiUrl}/${owner}/${name}/files?branch=${branch}`);
  }

  getCommits(owner: string, name: string, branch: string = 'main'): Observable<any[]> {
    return this.http.get<RepositoryCommit[]>(`${this.apiUrl}/${owner}/${name}/commits?branch=${branch}`);
  }

  getCommitDetails(owner: string, name: string, hash: string): Observable<RepositoryCommit> {
    return this.http.get<RepositoryCommit>(`${this.apiUrl}/${owner}/${name}/commits/${hash}`);
  }

  getFileContent(owner: string, name: string, path: string, branch: string = 'main'): Observable<{content: string}> {
    return this.http.get<{content: string}>(`${this.apiUrl}/${owner}/${name}/content?path=${path}&branch=${branch}`);
  }

  star(owner: string, name: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.apiUrl}/${owner}/${name}/star`, {});
  }

  unstar(owner: string, name: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.apiUrl}/${owner}/${name}/unstar`, {});
  }

  fork(owner: string, name: string, newOwner: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.apiUrl}/${owner}/${name}/fork?newOwner=${newOwner}`, {});
  }

  updateRepository(owner: string, name: string, payload: Partial<Repository>): Observable<Repository> {
    return this.http.put<Repository>(`${this.apiUrl}/${owner}/${name}`, payload);
  }

  createBranch(owner: string, name: string, branchName: string, fromBranch: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/${owner}/${name}/branches`, { name: branchName, fromBranch });
  }

  mergeBranch(owner: string, name: string, sourceBranch: string, targetBranch: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/${owner}/${name}/merge`, { sourceBranch, targetBranch });
  }

  listAccess(owner: string, name: string): Observable<RepositoryAccess[]> {
    return this.http.get<RepositoryAccess[]>(`${this.apiUrl}/${owner}/${name}/access`);
  }

  upsertAccess(owner: string, name: string, payload: RepositoryAccess): Observable<RepositoryAccess> {
    return this.http.post<RepositoryAccess>(`${this.apiUrl}/${owner}/${name}/access`, payload);
  }

  revokeAccess(owner: string, name: string, actorKey: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${owner}/${name}/access/${encodeURIComponent(actorKey)}`);
  }
}
