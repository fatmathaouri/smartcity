import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AgentService {
  private apiUrl = '/api/agent';

  constructor(private http: HttpClient) {}

  getMyTasks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tasks`);
  }

  getPendingTasks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tasks/pending`);
  }

  getInProgressTasks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tasks/in-progress`);
  }

  getResolvedTasks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tasks/resolved`);
  }

  getMyProfile(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/me`);
  }

  getMyPerformance(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/performance`);
  }

  toggleDisponibilite(): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/disponibilite`, {});
  }
}
