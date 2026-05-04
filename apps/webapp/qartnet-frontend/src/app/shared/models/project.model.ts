export interface Project {
  id?: number;
  title: string;
  description: string;
  repositoryId?: number | null;
  repositoryName: string;
  deadline: string;
  progress: number;
  teamCount: number;
  phases?: ProjectPhase[];
  team?: ProjectMember[];
  timeline?: ProjectTimelineItem[];
}

export interface ProjectPhase {
  id?: number;
  name: string;
  status: string;
  completed: boolean;
}

export interface ProjectMember {
  id?: number;
  userId: string;
  name: string;
  role: string;
  avatarUrl?: string;
}

export interface ProjectTimelineItem {
  id?: number;
  date: string;
  event: string;
  icon?: string;
  color?: string;
}
