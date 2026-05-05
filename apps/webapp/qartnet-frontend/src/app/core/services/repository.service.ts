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

  private repositoryUrl(owner: string, name: string): string {
    return `${this.apiUrl}/${encodeURIComponent(owner)}/${encodeURIComponent(name)}`;
  }

  private queryValue(value: string): string {
    return encodeURIComponent(value);
  }

  getAllRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(this.apiUrl);
  }

  getByOwnerAndName(owner: string, name: string): Observable<Repository> {
    return this.http.get<Repository>(this.repositoryUrl(owner, name));
  }

  createRepository(repository: Repository): Observable<Repository> {
    return this.http.post<Repository>(this.apiUrl, repository);
  }

  deleteRepository(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getBranches(owner: string, name: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.repositoryUrl(owner, name)}/branches`);
  }

  getFiles(owner: string, name: string, branch: string = 'main', path: string = ''): Observable<RepositoryFile[]> {
    let url = `${this.repositoryUrl(owner, name)}/files?branch=${this.queryValue(branch)}`;
    if (path) url += `&path=${this.queryValue(path)}`;
    return this.http.get<RepositoryFile[]>(url);
  }

  getCommits(owner: string, name: string, branch: string = 'main'): Observable<any[]> {
    return this.http.get<RepositoryCommit[]>(`${this.repositoryUrl(owner, name)}/commits?branch=${this.queryValue(branch)}`);
  }

  getCommitDetails(owner: string, name: string, hash: string): Observable<RepositoryCommit> {
    return this.http.get<RepositoryCommit>(`${this.repositoryUrl(owner, name)}/commits/${encodeURIComponent(hash)}`);
  }

  getFileContent(owner: string, name: string, path: string, branch: string = 'main'): Observable<{content: string}> {
    return this.http.get<{content: string}>(`${this.repositoryUrl(owner, name)}/content?path=${this.queryValue(path)}&branch=${this.queryValue(branch)}`);
  }

  star(owner: string, name: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.repositoryUrl(owner, name)}/star`, {});
  }

  unstar(owner: string, name: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.repositoryUrl(owner, name)}/unstar`, {});
  }

  fork(owner: string, name: string, newOwner: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.repositoryUrl(owner, name)}/fork?newOwner=${this.queryValue(newOwner)}`, {});
  }

  updateRepository(owner: string, name: string, payload: Partial<Repository>): Observable<Repository> {
    return this.http.put<Repository>(this.repositoryUrl(owner, name), payload);
  }

  createBranch(owner: string, name: string, branchName: string, fromBranch: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.repositoryUrl(owner, name)}/branches`, { name: branchName, fromBranch });
  }

  mergeBranch(owner: string, name: string, sourceBranch: string, targetBranch: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.repositoryUrl(owner, name)}/merge`, { sourceBranch, targetBranch });
  }

  listAccess(owner: string, name: string): Observable<RepositoryAccess[]> {
    return this.http.get<RepositoryAccess[]>(`${this.repositoryUrl(owner, name)}/access`);
  }

  upsertAccess(owner: string, name: string, payload: RepositoryAccess): Observable<RepositoryAccess> {
    return this.http.post<RepositoryAccess>(`${this.repositoryUrl(owner, name)}/access`, payload);
  }

  revokeAccess(owner: string, name: string, actorKey: string): Observable<void> {
    return this.http.delete<void>(`${this.repositoryUrl(owner, name)}/access/${encodeURIComponent(actorKey)}`);
  }

  deleteBranch(owner: string, name: string, branchName: string): Observable<void> {
    return this.http.delete<void>(`${this.repositoryUrl(owner, name)}/branches/${encodeURIComponent(branchName)}`);
  }

  setDefaultBranch(owner: string, name: string, branch: string): Observable<Repository> {
    return this.http.put<Repository>(`${this.repositoryUrl(owner, name)}/default-branch?branch=${this.queryValue(branch)}`, {});
  }

  watch(owner: string, name: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.repositoryUrl(owner, name)}/watch`, {});
  }

  unwatch(owner: string, name: string): Observable<Repository> {
    return this.http.post<Repository>(`${this.repositoryUrl(owner, name)}/unwatch`, {});
  }
}
