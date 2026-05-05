export interface Repository {
  id?: number;
  owner: string;
  ownerDisplayName: string;
  name: string;
  description: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  language?: string;
  stars?: number;
  forks?: number;
  watchers?: number;
  updatedAt?: string;
  readmeTitle?: string;
  readmeSubtitle?: string;
  cloneUrl?: string;
  defaultBranch?: string;
}

export interface RepositoryCommit {
  hash: string;
  message: string;
  author: string;
  date: string;
  modifiedPaths?: string[];
  additions?: number;
  deletions?: number;
}

export interface RepositoryAccess {
  id?: number;
  actorKey: string;
  role: 'OWNER' | 'COLLABORATOR';
  level: 'READ' | 'WRITE';
}

export interface RepositoryFile {
  id?: number;
  name: string;
  type: string;
  message: string;
  updatedAt: string;
}

export interface RepositoryMetric {
  id?: number;
  totalCommits: number;
  totalBranches: number;
  totalContributors: number;
  totalFiles: number;
}
