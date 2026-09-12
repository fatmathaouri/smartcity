import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PlanningService {
  private apiUrl = 'http://localhost:8081/api/planning';

  constructor(private http: HttpClient) {}

  getMyPlans(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/my`);
  }

  getAgentPlans(agentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/agent/${agentId}`);
  }

  getWeekOverview(agentId: number | null, weekStart: string): Observable<any> {
    let params = new HttpParams().set('weekStart', weekStart);
    if (agentId) params = params.set('agentId', agentId.toString());
    return this.http.get<any>(`${this.apiUrl}/week`, { params });
  }

  createPlan(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, data);
  }

  updatePlan(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, data);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    });
  }

  validatePlan(id: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/validate`, {});
  }

  rejectPlan(id: number, reason?: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/reject`, reason ? { reason } : {});
  }

  getCompletedPlans(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/completed`);
  }

  getPlanningStats(weekStart: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`, {
      params: new HttpParams().set('weekStart', weekStart)
    });
  }

  deletePlan(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
