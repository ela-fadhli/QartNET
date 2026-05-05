export interface Project {
  id?: number;
  title: string;
  description: string;
  repositoryId?: number | null;
  repositoryName: string;
  deadline: string;
  progress: number;
  teamCount: number;
  creatorName?: string;
  creatorUsername?: string;
  phases?: ProjectPhase[];
  team?: ProjectMember[];
  timeline?: ProjectTimelineItem[];
}

export interface ProjectPhase {
  id?: number;
  title: string;
  status: string;
  tasks?: ProjectTask[];
}

export interface ProjectTask {
  id: number;
  title: string;
  status: string;
  assigneeInitials?: string;
  dueDate?: string;
}

export interface ProjectMember {
  id?: number;
  userId: string;
  name: string;
  role: string;
  specialty?: string;
  initials?: string;
  avatarUrl?: string;
}

export interface ProjectTimelineItem {
  id?: number;
  date: string;
  title: string;
  icon?: string;
  color?: string;
}
