export interface ForumSummaryResponse {
  publicId: string;
  name: string;
  slug: string;
  description: string | null;
  banner: string | null;
  ownerUsername: string;
  threadCount: number;
  createdAt: string;
}

export interface ForumCategoryResponse {
  publicId: string;
  name: string;
}

export interface ForumMemberResponse {
  userPublicId: string;
  username: string;
  role: 'ADMIN' | 'MODERATOR';
}

export interface ForumDetailResponse {
  publicId: string;
  name: string;
  slug: string;
  description: string | null;
  banner: string | null;
  ownerUsername: string;
  categories: ForumCategoryResponse[];
  threadCount: number;
  createdAt: string;
  admin: boolean;
  moderator: boolean;
}

export interface TagResponse {
  publicId: string;
  name: string;
}

export interface ThreadSummaryResponse {
  publicId: string;
  title: string;
  authorUsername: string;
  forumSlug: string;
  forumName: string;
  category: ForumCategoryResponse;
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
  forumSlug: string;
  forumName: string;
  category: ForumCategoryResponse;
  tags: TagResponse[];
  viewCount: number;
  replies: ReplyResponse[];
  createdAt: string;
}

export interface CreateForumRequest {
  name: string;
  slug: string;
  description: string | null;
  banner: string | null;
}

export interface CreateThreadRequest {
  title: string;
  body: string;
  categoryPublicId: string;
  tagNames: string[];
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
