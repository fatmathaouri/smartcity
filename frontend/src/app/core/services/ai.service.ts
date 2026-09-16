import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClassificationResult, DuplicateResult, ZonePrediction } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class AiService {
  private apiUrl = '/api/ai';

  constructor(private http: HttpClient) {}

  classify(text: string): Observable<ClassificationResult> {
    return this.http.post<ClassificationResult>(`${this.apiUrl}/classify`, text);
  }

  findDuplicates(reportId: number): Observable<DuplicateResult[]> {
    return this.http.get<DuplicateResult[]>(`${this.apiUrl}/duplicates/${reportId}`);
  }

  getPredictions(): Observable<ZonePrediction[]> {
    return this.http.get<ZonePrediction[]>(`${this.apiUrl}/predictions`);
  }
}
