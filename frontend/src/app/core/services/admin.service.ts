import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateUserRequest, UserCreationResult, Department, RoleAssignment } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = '/api/admin';
  private deptUrl = '/api/departments';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  getUserById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/users/${id}`);
  }

  createUser(request: CreateUserRequest): Observable<UserCreationResult> {
    return this.http.post<UserCreationResult>(`${this.apiUrl}/users`, request);
  }

  resetTempPassword(userId: number): Observable<UserCreationResult> {
    return this.http.post<UserCreationResult>(`${this.apiUrl}/users/${userId}/reset-password`, {});
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}/role`, null, {
      params: { role }
    });
  }

  removeUserRole(userId: number, role: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/users/${userId}/role`, {
      params: { role }
    });
  }

  toggleUserEnabled(userId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}/toggle`, {});
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${userId}`);
  }

  getRoleAssignments(): Observable<RoleAssignment[]> {
    return this.http.get<RoleAssignment[]>(`${this.apiUrl}/role-assignments`);
  }

  createCategory(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/categories`, data);
  }

  updateCategory(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/categories/${id}`, data);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }

  getGlobalStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`);
  }

  getHeatmapData(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/heatmap`);
  }

  getReportsByMonth(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/reports-by-month`);
  }

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(this.deptUrl);
  }

  getDepartmentsManagerStatus(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/departments-manager-status`);
  }

  reassignManager(userId: number, departmentId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/managers/${userId}/department`, null, {
      params: { departmentId: departmentId.toString() }
    });
  }

  createDepartment(data: { name: string; description: string; categoryId: number }): Observable<Department> {
    return this.http.post<Department>(this.deptUrl, data);
  }

  updateDepartment(id: number, data: { name: string; description: string; categoryId: number }): Observable<Department> {
    return this.http.put<Department>(`${this.deptUrl}/${id}`, data);
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.deptUrl}/${id}`);
  }
}
