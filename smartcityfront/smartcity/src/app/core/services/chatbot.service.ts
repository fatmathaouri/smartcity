import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private apiUrl = 'http://localhost:8081/api/ai/chat';

  constructor(private http: HttpClient) {}

  chat(message: string): Observable<any> {
    return this.http.post<any>(this.apiUrl, { message });
  }
}
