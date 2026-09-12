import { Component, OnInit } from '@angular/core';
import { DepartmentService } from '../../../core/services/department.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-manager-dashboard',
  templateUrl: './manager-dashboard.component.html',
  styleUrls: ['./manager-dashboard.component.css']
})
export class ManagerDashboardComponent implements OnInit {
  profile: any = null;
  stats: any = null;
  agents: any[] = [];
  performances: any[] = [];
  unassignedReports: any[] = [];
  suggestedAgent: any = null;
  selectedReportId: number | null = null;
  loading = true;

  showAgentForm = false;
  agentForm = { email: '', firstName: '', lastName: '', phone: '' };
  agentError = '';
  createdPassword = '';

  editAgentId: number | null = null;
  editForm = { zone: '', specialty: '' };

  activeTab = 'agents';

  constructor(
    private departmentService: DepartmentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.departmentService.getProfile().subscribe(p => {
      this.profile = p;
      this.departmentService.getStats().subscribe(s => this.stats = s);
      this.departmentService.getAgents().subscribe(a => this.agents = a);
      this.departmentService.getAgentPerformances().subscribe(p => this.performances = p);
      this.departmentService.getUnassignedReports().subscribe(r => this.unassignedReports = r);
      this.loading = false;
    });
  }

  createAgent(): void {
    this.agentError = '';
    this.departmentService.createAgent({
      ...this.agentForm,
      departmentId: this.profile?.departmentId
    }).subscribe({
      next: (res) => {
        this.createdPassword = res.temporaryPassword;
        this.showAgentForm = false;
        this.agentForm = { email: '', firstName: '', lastName: '', phone: '' };
        this.loadData();
      },
      error: (err) => this.agentError = err.error?.error || 'Erreur lors de la création'
    });
  }

  startEditAgent(agent: any): void {
    this.editAgentId = agent.id;
    this.editForm = { zone: agent.zone || '', specialty: agent.specialty || '' };
  }

  saveEditAgent(): void {
    if (this.editAgentId === null) return;
    this.departmentService.updateAgent(this.editAgentId, this.editForm.zone, this.editForm.specialty)
      .subscribe(() => {
        this.editAgentId = null;
        this.loadData();
      });
  }

  cancelEdit(): void {
    this.editAgentId = null;
  }

  deleteAgent(agent: any): void {
    if (!confirm(`Supprimer l'agent ${agent.userName} ? Cette action est irréversible.`)) return;
    this.departmentService.deleteAgent(agent.id).subscribe(() => this.loadData());
  }

  suggestAgentForReport(reportId: number): void {
    this.selectedReportId = reportId;
    this.departmentService.suggestAgent(reportId).subscribe(s => this.suggestedAgent = s);
  }

  assignAgentToReport(reportId: number, agentUserId: number): void {
    this.departmentService.assignAgent(reportId, agentUserId).subscribe(() => {
      this.suggestedAgent = null;
      this.selectedReportId = null;
      this.loadData();
    });
  }

  dismissCreatedPassword(): void {
    this.createdPassword = '';
  }

  getResolutionColor(rate: number): string {
    if (rate >= 80) return '#10b981';
    if (rate >= 50) return '#f59e0b';
    return '#dc2626';
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return '#dc2626';
      case 'HIGH': return '#f59e0b';
      case 'MEDIUM': return '#3b82f6';
      default: return '#6b7280';
    }
  }
}
