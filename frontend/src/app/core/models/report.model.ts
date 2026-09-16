export interface Report {
  id: number;
  title: string;
  description?: string;
  status: string;
  priority?: string;
  latitude?: number;
  longitude?: number;
  photoUrl?: string;
  address?: string;
  createdAt: string;
  updatedAt?: string;
  citizenId: number;
  citizenName: string;
  categoryId?: number;
  categoryName?: string;
  slaDeadline?: string;
  resolvedAt?: string;
  slaBreached?: boolean;
  hoursRemaining?: number;
  photoUrls?: string[];
  videoUrl?: string;
  proofPhoto?: string;
  treatmentStatus?: string;
  assignedToId?: number;
  assignedToName?: string;
  photoBefore?: string;
  photoAfter?: string;
  agentComment?: string;
  interventionDate?: string;
  likeCount?: number;
  confirmCount?: number;
  commentCount?: number;
  likedByMe?: boolean;
  confirmedByMe?: boolean;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  defaultPriority: string;
  slaHours?: number;
  escalationHours?: number;
}

export interface DashboardStats {
  totalReports: number;
  pendingReports: number;
  inProgressReports: number;
  resolvedReports: number;
  reportsByCategory: { [key: string]: number };
  reportsByMonth: { [key: number]: number };
  predictions: ZonePrediction[];
}

export interface ZonePrediction {
  zoneName: string;
  incidentIncrease: number;
  recommendation: string;
}

export interface DuplicateResult {
  reportId: number;
  title: string;
  similarity: number;
}

export interface ClassificationResult {
  category: string;
  priority: string;
  score: number;
}

export interface Notification {
  id: number;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
  citizen: any;
  user: any;
  report: any;
}
