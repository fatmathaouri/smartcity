import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Report } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = 'http://localhost:8081/api/reports';
  private slaUrl = 'http://localhost:8081/api/sla';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Report[]> {
    return this.http.get<Report[]>(this.apiUrl);
  }

  getById(id: number): Observable<Report> {
    return this.http.get<Report>(`${this.apiUrl}/${id}`);
  }

  getMyReports(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.apiUrl}/my`);
  }

  getReportsByDepartment(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.apiUrl}/by-department`);
  }

  getByAgent(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.apiUrl}/by-agent`);
  }

  create(report: Partial<Report>): Observable<Report> {
    return this.http.post<Report>(this.apiUrl, report);
  }

  update(id: number, report: Partial<Report>): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}`, report);
  }

  updateStatus(id: number, status: string): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    });
  }

  updateTreatmentStatus(id: number, status: string): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}/treatment-status`, null, {
      params: new HttpParams().set('status', status)
    });
  }

  assignAgent(reportId: number, agentId: number): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${reportId}/assign`, null, {
      params: new HttpParams().set('agentId', agentId.toString())
    });
  }

  agentIntervene(id: number, body: any): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}/intervene`, body);
  }

  resolveReport(id: number): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}/resolve`, null);
  }

  validateReport(id: number): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}/validate`, null);
  }

  vote(id: number, type: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/vote`, null, {
      params: new HttpParams().set('type', type)
    });
  }

  addComment(id: number, content: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/comments`, { content });
  }

  getComments(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/comments`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getNearby(lat: number, lng: number, radius: number = 5): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.apiUrl}/nearby`, {
      params: new HttpParams()
        .set('lat', lat.toString())
        .set('lng', lng.toString())
        .set('radius', radius.toString())
    });
  }

  addPhoto(id: number, file: File): Observable<Report> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Report>(`${this.apiUrl}/${id}/photos`, formData);
  }

  setProofPhoto(id: number, file: File): Observable<Report> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<Report>(`${this.apiUrl}/${id}/proof`, formData);
  }

  addVideo(id: number, file: File): Observable<Report> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<Report>(`${this.apiUrl}/${id}/video`, formData);
  }

  getSlaStats(): Observable<any> {
    return this.http.get<any>(`${this.slaUrl}/stats`);
  }

  getSlaConfig(): Observable<any[]> {
    return this.http.get<any[]>(`${this.slaUrl}/config`);
  }

  updateSlaConfig(categoryId: number, slaHours: number, escalationHours: number): Observable<any> {
    return this.http.put<any>(`${this.slaUrl}/config/${categoryId}`, { slaHours, escalationHours });
  }
}
