export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  roles: string[];
  mustChangePassword: boolean;
  enabled: boolean;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  mustChangePassword: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  city: string;
}

export interface PasswordChangeRequest {
  oldPassword: string;
  newPassword: string;
}

export interface Department {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
  categoryId: number;
  categoryName: string;
  agentCount: number;
}

export interface UserCreationResult {
  userId: number;
  email: string;
  username: string;
  temporaryPassword: string;
  role: string;
  departmentName: string;
  createdAt: string;
}

export interface CreateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  roleName: string;
  departmentId: number;
}

export interface RoleAssignment {
  id: number;
  user: User;
  role: string;
  assignedBy: string;
  assignedAt: string;
  revokedAt: string;
}
