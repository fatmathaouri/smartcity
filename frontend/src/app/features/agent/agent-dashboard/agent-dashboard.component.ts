import { Component, OnInit } from '@angular/core';
import { AgentService } from '../../../core/services/agent.service';
import { ReportService } from '../../../core/services/report.service';
import { AuthService } from '../../../core/services/auth.service';
import { PlanningService } from '../../../core/services/planning.service';

@Component({
  selector: 'app-agent-dashboard',
  templateUrl: './agent-dashboard.component.html',
  styleUrls: ['./agent-dashboard.component.css']
})
export class AgentDashboardComponent implements OnInit {
  profile: any = null;
  performance: any = null;
  tasks: any[] = [];
  filteredTasks: any[] = [];
  activeFilter = 'all';
  selectedTask: any = null;
  interventionComment = '';
  proofPhotoFile: File | null = null;
  photoBeforeFile: File | null = null;
  photoAfterFile: File | null = null;
  loading = true;

  activeDashTab: string = 'tasks';
  myPlans: any[] = [];
  todayPlans: any[] = [];
  upcomingPlans: any[] = [];

  constructor(
    private agentService: AgentService,
    private reportService: ReportService,
    public authService: AuthService,
    private planningService: PlanningService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.agentService.getMyProfile().subscribe(p => this.profile = p);
    this.agentService.getMyPerformance().subscribe(p => this.performance = p);
    this.agentService.getMyTasks().subscribe(t => {
      this.tasks = t;
      this.filteredTasks = t;
      this.loading = false;
    });
  }

  filterTasks(filter: string): void {
    this.activeFilter = filter;
    if (filter === 'all') {
      this.filteredTasks = this.tasks;
    } else {
      this.filteredTasks = this.tasks.filter(t => t.treatmentStatus === filter);
    }
  }

  selectTask(task: any): void {
    this.selectedTask = { ...task };
    this.interventionComment = '';
    this.proofPhotoFile = null;
    this.photoBeforeFile = null;
    this.photoAfterFile = null;
  }

  acceptTask(task: any): void {
    this.reportService.updateTreatmentStatus(task.id, 'ACCEPTED').subscribe(() => {
      this.loadData();
      this.selectedTask = null;
    });
  }

  startIntervention(task: any): void {
    const body: any = { comment: this.interventionComment };
    this.reportService.agentIntervene(task.id, body).subscribe(() => {
      const uploads: Promise<any>[] = [];
      if (this.photoBeforeFile) {
        uploads.push(this.reportService.addPhoto(task.id, this.photoBeforeFile).toPromise());
      }
      if (this.photoAfterFile) {
        uploads.push(this.reportService.addPhoto(task.id, this.photoAfterFile).toPromise());
      }
      Promise.all(uploads).then(() => {
        this.loadData();
        this.selectedTask = null;
      }).catch(() => {
        this.loadData();
        this.selectedTask = null;
      });
    });
  }

  resolveTask(task: any): void {
    this.reportService.resolveReport(task.id).subscribe(() => {
      if (this.proofPhotoFile) {
        this.reportService.setProofPhoto(task.id, this.proofPhotoFile).toPromise().then(() => {
          this.loadData();
          this.selectedTask = null;
        });
      } else {
        this.loadData();
        this.selectedTask = null;
      }
    });
  }

  toggleDispo(): void {
    this.agentService.toggleDisponibilite().subscribe(p => this.profile = p);
  }

  onProofPhotoSelected(event: any): void {
    this.proofPhotoFile = event.target.files[0];
  }

  onPhotoBeforeSelected(event: any): void {
    this.photoBeforeFile = event.target.files[0];
  }

  onPhotoAfterSelected(event: any): void {
    this.photoAfterFile = event.target.files[0];
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'NEW': 'Nouveau',
      'ACCEPTED': 'Accepté',
      'IN_PROGRESS': 'En intervention',
      'RESOLVED': 'Résolu',
      'VALIDATED': 'Validé'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      'NEW': 'status-new',
      'ACCEPTED': 'status-accepted',
      'IN_PROGRESS': 'status-progress',
      'RESOLVED': 'status-resolved',
      'VALIDATED': 'status-validated'
    };
    return classes[status] || '';
  }

  getPriorityClass(priority: string): string {
    if (priority === 'HIGH') return 'priority-high';
    if (priority === 'MEDIUM') return 'priority-medium';
    return 'priority-low';
  }

  loadPlans(): void {
    this.planningService.getMyPlans().subscribe(plans => {
      this.myPlans = plans;
      const today = new Date().toISOString().split('T')[0];
      this.todayPlans = plans.filter(p => p.plannedDate.split('T')[0] === today
        && (p.status === 'PLANNED' || p.status === 'IN_PROGRESS'));
      this.upcomingPlans = plans.filter(p => p.plannedDate.split('T')[0] > today
        && p.status === 'PLANNED');
    });
  }

  switchDashTab(tab: string): void {
    this.activeDashTab = tab;
    if (tab === 'planning') this.loadPlans();
  }

  startPlan(plan: any): void {
    this.planningService.updateStatus(plan.id, 'IN_PROGRESS').subscribe(() => {
      this.loadPlans();
    });
  }

  completePlan(plan: any): void {
    this.planningService.updateStatus(plan.id, 'COMPLETED').subscribe(() => {
      this.loadPlans();
    });
  }

  getPlanStatusLabel(status: string): string {
    const l: Record<string, string> = {
      'PLANNED': 'Planifié', 'IN_PROGRESS': 'En cours', 'COMPLETED': 'Terminé',
      'VALIDATED': 'Validé', 'CANCELLED': 'Annulé'
    };
    return l[status] || status;
  }

  getPlanStatusClass(status: string): string {
    const c: Record<string, string> = {
      'PLANNED': 'status-planned', 'IN_PROGRESS': 'status-progress',
      'COMPLETED': 'status-completed', 'VALIDATED': 'status-validated',
      'CANCELLED': 'status-cancelled'
    };
    return c[status] || '';
  }
}
