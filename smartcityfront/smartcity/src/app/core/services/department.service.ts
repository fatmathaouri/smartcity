import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private apiUrl = 'http://localhost:8081/api/department';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/me`);
  }

  getAgents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/agents`);
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`);
  }

  getAgentPerformances(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/agent-performances`);
  }

  addAgent(userId: number, zone: string, specialty: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/agents`, null, {
      params: { userId: userId.toString(), zone, specialty }
    });
  }

  assignAgent(reportId: number, agentId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/assign`, null, {
      params: { reportId: reportId.toString(), agentId: agentId.toString() }
    });
  }

  suggestAgent(reportId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/suggest-agent`, {
      params: { reportId: reportId.toString() }
    });
  }

  getUnassignedReports(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/unassigned-reports`);
  }

  deleteAgent(agentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/agents/${agentId}`);
  }

  updateAgent(agentId: number, zone?: string, specialty?: string): Observable<any> {
    const params: any = {};
    if (zone) params.zone = zone;
    if (specialty) params.specialty = specialty;
    return this.http.put<any>(`${this.apiUrl}/agents/${agentId}`, null, { params });
  }

  createAgent(agent: { email: string; firstName: string; lastName: string; phone: string; departmentId: number }): Observable<any> {
    return this.http.post<any>('http://localhost:8081/api/agent/create', {
      email: agent.email,
      firstName: agent.firstName,
      lastName: agent.lastName,
      phone: agent.phone,
      departmentId: agent.departmentId
    });
  }
}
