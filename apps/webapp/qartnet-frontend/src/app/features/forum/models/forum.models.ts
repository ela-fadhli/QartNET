export interface CategoryResponse {
  publicId: string;
  name: string;
  description: string | null;
}

export interface TagResponse {
  publicId: string;
  name: string;
}

export interface ThreadSummaryResponse {
  publicId: string;
  title: string;
  authorUsername: string;
  category: CategoryResponse;
  tags: TagResponse[];
  viewCount: number;
  replyCount: number;
  createdAt: string;
}

export interface ReplyResponse {
  publicId: string;
  body: string;
  authorUsername: string;
  parentReplyPublicId: string | null;
  createdAt: string;
}

export interface ThreadDetailResponse {
  publicId: string;
  title: string;
  body: string;
  authorUsername: string;
  category: CategoryResponse;
  tags: TagResponse[];
  viewCount: number;
  replies: ReplyResponse[];
  createdAt: string;
}

export interface CreateThreadRequest {
  title: string;
  body: string;
  categoryPublicId: string;
  tagPublicIds: string[];
}

export interface CreateReplyRequest {
  body: string;
  parentReplyPublicId: string | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
