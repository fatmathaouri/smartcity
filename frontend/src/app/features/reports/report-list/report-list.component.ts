import { Component, OnInit } from '@angular/core';
import { ReportService } from '../../../core/services/report.service';
import { Report } from '../../../core/models/report.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {
  reports: Report[] = [];
  filterStatus = '';

  constructor(
    private reportService: ReportService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    const user = this.authService.getCurrentUser();
    if (user?.roles?.includes('ROLE_CITIZEN')) {
      this.reportService.getMyReports().subscribe(data => this.reports = data);
    } else if (user?.roles?.includes('ROLE_DEPARTMENT_MANAGER')) {
      this.reportService.getReportsByDepartment().subscribe(data => this.reports = data);
    } else if (user?.roles?.includes('ROLE_MUNICIPAL_AGENT')) {
      this.reportService.getByAgent().subscribe(data => this.reports = data);
    } else {
      this.reportService.getAll().subscribe(data => this.reports = data);
    }
  }

  get filteredReports(): Report[] {
    if (!this.filterStatus) return this.reports;
    return this.reports.filter(r => r.status === this.filterStatus);
  }
}
