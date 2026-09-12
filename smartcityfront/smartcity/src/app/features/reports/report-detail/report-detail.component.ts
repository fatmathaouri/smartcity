import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReportService } from '../../../core/services/report.service';
import { AiService } from '../../../core/services/ai.service';
import { AuthService } from '../../../core/services/auth.service';
import { Report, DuplicateResult } from '../../../core/models/report.model';

@Component({
  selector: 'app-report-detail',
  templateUrl: './report-detail.component.html',
  styleUrls: ['./report-detail.component.css']
})
export class ReportDetailComponent implements OnInit {
  report: Report | null = null;
  duplicates: DuplicateResult[] = [];
  comments: any[] = [];
  newComment = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reportService: ReportService,
    private aiService: AiService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.reportService.getById(id).subscribe(data => {
      this.report = data;
      this.checkDuplicates(id);
      this.loadComments(id);
    });
  }

  checkDuplicates(id: number): void {
    this.aiService.findDuplicates(id).subscribe(data => this.duplicates = data);
  }

  loadComments(id: number): void {
    this.reportService.getComments(id).subscribe(data => this.comments = data);
  }

  toggleVote(type: string): void {
    if (!this.report) return;
    this.reportService.vote(this.report.id, type).subscribe((res: any) => {
      if (type === 'LIKE') {
        this.report!.likeCount = res.likeCount;
        this.report!.likedByMe = res.added;
      } else {
        this.report!.confirmCount = res.confirmCount;
        this.report!.confirmedByMe = res.added;
      }
    });
  }

  addComment(): void {
    if (!this.report || !this.newComment.trim()) return;
    this.reportService.addComment(this.report.id, this.newComment.trim()).subscribe(comment => {
      this.comments.unshift(comment);
      this.report!.commentCount = (this.report!.commentCount || 0) + 1;
      this.newComment = '';
    });
  }

  updateStatus(status: string): void {
    if (this.report) {
      this.reportService.updateStatus(this.report.id, status).subscribe(data => this.report = data);
    }
  }

  resolve(): void {
    if (this.report) {
      this.reportService.resolveReport(this.report.id).subscribe(data => this.report = data);
    }
  }

  validate(): void {
    if (this.report) {
      this.reportService.validateReport(this.report.id).subscribe(data => this.report = data);
    }
  }

  delete(): void {
    if (this.report && confirm('Supprimer ce signalement ?')) {
      this.reportService.delete(this.report.id).subscribe(() => this.router.navigate(['/reports']));
    }
  }

  getSlaPercentage(): number {
    if (!this.report?.slaDeadline) return 100;
    const now = new Date().getTime();
    const created = new Date(this.report.createdAt).getTime();
    const deadline = new Date(this.report.slaDeadline).getTime();
    const total = deadline - created;
    const remaining = deadline - now;
    if (total <= 0) return 0;
    return Math.max(0, Math.min(100, Math.round((remaining / total) * 100)));
  }

  getSlaColor(): string {
    const pct = this.getSlaPercentage();
    if (this.report?.slaBreached) return '#f44336';
    if (pct > 50) return '#4caf50';
    if (pct > 25) return '#ff9800';
    return '#f44336';
  }

  getSlaStatusText(): string {
    if (this.report?.slaBreached) return 'EN RETARD';
    const pct = this.getSlaPercentage();
    if (pct > 50) return 'Dans les temps';
    if (pct > 25) return 'Attention';
    return 'Critique';
  }

  getAllPhotoUrls(): string[] {
    if (!this.report) return [];
    const urls: string[] = [];
    if (this.report.photoUrl) urls.push(this.report.photoUrl);
    if (this.report.photoUrls) urls.push(...this.report.photoUrls);
    return urls;
  }

  get isCitizen(): boolean {
    return this.authService.hasRole('ROLE_CITIZEN');
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_MUNICIPALITY');
  }

  get isAgent(): boolean {
    return this.authService.hasRole('ROLE_MUNICIPAL_AGENT') || this.isAdmin;
  }

  getPriorityLabel(p: string | undefined): string {
    if (!p) return 'Non définie';
    switch (p) {
      case 'CRITICAL': return 'Critique';
      case 'HIGH': return 'Haute';
      case 'MEDIUM': return 'Moyenne';
      case 'LOW': return 'Basse';
      default: return 'Non définie';
    }
  }
}
