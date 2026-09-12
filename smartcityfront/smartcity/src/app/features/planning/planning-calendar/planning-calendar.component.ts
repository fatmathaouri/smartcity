import { Component, OnInit } from '@angular/core';
import { PlanningService } from '../../../core/services/planning.service';
import { DepartmentService } from '../../../core/services/department.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-planning-calendar',
  templateUrl: './planning-calendar.component.html',
  styleUrls: ['./planning-calendar.component.css']
})
export class PlanningCalendarComponent implements OnInit {
  weekStart: Date = new Date();
  weekDays: Date[] = [];
  plans: any[] = [];
  plansByDay: Map<string, any[]> = new Map();
  overview: any = null;
  selectedPlan: any = null;
  timeSlots = ['08:00-10:00', '09:00-12:00', '10:00-13:00', '14:00-16:00', '14:00-17:00', '16:00-18:00'];

  showForm = false;
  form = { reportId: 0, agentId: 0, plannedDate: '', timeSlot: '09:00-12:00', notes: '' };
  unassignedReports: any[] = [];
  agents: any[] = [];
  conflictError = '';
  formSuccess = '';

  activeTab: 'calendar' | 'completed' | 'stats' = 'calendar';
  completedPlans: any[] = [];
  planningStats: any = null;

  agentFilter: number | null = null;
  isManager = false;
  isAgent = false;
  isAgentOnly = false;

  constructor(
    private planningService: PlanningService,
    private departmentService: DepartmentService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isManager = this.authService.hasRole('ROLE_DEPARTMENT_MANAGER')
      || this.authService.hasRole('ROLE_ADMIN')
      || this.authService.hasRole('ROLE_MUNICIPALITY');
    this.isAgent = this.authService.hasRole('ROLE_MUNICIPAL_AGENT');
    this.isAgentOnly = this.isAgent && !this.authService.hasRole('ROLE_DEPARTMENT_MANAGER')
      && !this.authService.hasRole('ROLE_ADMIN') && !this.authService.hasRole('ROLE_MUNICIPALITY');
    this.goToThisWeek();
    if (this.isManager) {
      this.loadFormData();
    }
  }

  goToThisWeek(): void {
    const today = new Date();
    const day = today.getDay();
    const diff = today.getDate() - day + (day === 0 ? -6 : 1);
    this.weekStart = new Date(today);
    this.weekStart.setDate(diff);
    this.weekStart.setHours(0, 0, 0, 0);
    this.computeWeekDays();
    this.loadWeek();
  }

  prevWeek(): void {
    this.weekStart.setDate(this.weekStart.getDate() - 7);
    this.computeWeekDays();
    this.loadWeek();
  }

  nextWeek(): void {
    this.weekStart.setDate(this.weekStart.getDate() + 7);
    this.computeWeekDays();
    this.loadWeek();
  }

  computeWeekDays(): void {
    this.weekDays = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(this.weekStart);
      d.setDate(d.getDate() + i);
      this.weekDays.push(d);
    }
  }

  loadWeek(): void {
    const start = this.weekStart.toISOString().split('T')[0] + 'T00:00:00';
    this.planningService.getWeekOverview(this.agentFilter, start).subscribe(res => {
      this.overview = res;
      this.plans = res.plans || [];
      this.plansByDay = new Map();
      for (const plan of this.plans) {
        const dateKey = plan.plannedDate.split('T')[0];
        if (!this.plansByDay.has(dateKey)) this.plansByDay.set(dateKey, []);
        this.plansByDay.get(dateKey)!.push(plan);
      }
    });
    if (this.activeTab === 'completed') this.loadCompletedPlans();
    if (this.activeTab === 'stats') this.loadStats();
  }

  loadFormData(): void {
    this.departmentService.getUnassignedReports().subscribe(r => this.unassignedReports = r);
    this.departmentService.getAgents().subscribe(a => this.agents = a);
  }

  loadCompletedPlans(): void {
    this.planningService.getCompletedPlans().subscribe(p => this.completedPlans = p);
  }

  loadStats(): void {
    const start = this.weekStart.toISOString().split('T')[0];
    this.planningService.getPlanningStats(start).subscribe(s => this.planningStats = s);
  }

  switchTab(tab: 'calendar' | 'completed' | 'stats'): void {
    this.activeTab = tab;
    if (tab === 'completed') this.loadCompletedPlans();
    if (tab === 'stats') this.loadStats();
  }

  onAgentFilterChange(): void {
    this.loadWeek();
  }

  createPlan(): void {
    this.conflictError = '';
    this.formSuccess = '';
    if (!this.form.reportId || !this.form.agentId || !this.form.plannedDate) return;

    const payload = {
      reportId: this.form.reportId,
      agentId: this.form.agentId,
      plannedDate: this.form.plannedDate,
      timeSlot: this.form.timeSlot,
      notes: this.form.notes
    };

    this.planningService.createPlan(payload).subscribe({
      next: () => {
        this.formSuccess = 'Plan créé avec succès !';
        this.showForm = false;
        this.form = { reportId: 0, agentId: 0, plannedDate: '', timeSlot: '09:00-12:00', notes: '' };
        this.loadWeek();
        this.loadFormData();
        setTimeout(() => this.formSuccess = '', 3000);
      },
      error: (err) => {
        if (err.error?.error) {
          this.conflictError = err.error.error;
        } else {
          this.conflictError = 'Erreur lors de la création du plan';
        }
      }
    });
  }

  getPlansForDay(day: Date): any[] {
    const key = day.toISOString().split('T')[0];
    return this.plansByDay.get(key) || [];
  }

  isToday(day: Date): boolean {
    return day.toISOString().split('T')[0] === new Date().toISOString().split('T')[0];
  }

  getStatusLabel(status: string): string {
    const l: Record<string, string> = {
      'PLANNED': 'Planifié', 'IN_PROGRESS': 'En cours', 'COMPLETED': 'Terminé',
      'VALIDATED': 'Validé', 'CANCELLED': 'Annulé'
    };
    return l[status] || status;
  }

  getStatusClass(status: string): string {
    const c: Record<string, string> = {
      'PLANNED': 'status-planned', 'IN_PROGRESS': 'status-progress',
      'COMPLETED': 'status-completed', 'VALIDATED': 'status-validated',
      'CANCELLED': 'status-cancelled'
    };
    return c[status] || '';
  }

  openPlanDetail(plan: any): void {
    this.selectedPlan = plan;
  }

  updatePlanStatus(plan: any, status: string): void {
    this.planningService.updateStatus(plan.id, status).subscribe(() => {
      this.loadWeek();
      this.selectedPlan = null;
    });
  }

  validatePlan(plan: any): void {
    this.planningService.validatePlan(plan.id).subscribe(() => {
      this.loadWeek();
      this.loadCompletedPlans();
      this.selectedPlan = null;
    });
  }

  rejectPlan(plan: any): void {
    const reason = prompt('Motif du rejet (optionnel):');
    this.planningService.rejectPlan(plan.id, reason || undefined).subscribe(() => {
      this.loadWeek();
      this.loadCompletedPlans();
      this.selectedPlan = null;
    });
  }

  formatDate(day: Date): string {
    return day.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
  }

  getDayName(day: Date): string {
    return day.toLocaleDateString('fr-FR', { weekday: 'long' });
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return 'priority-critical';
      case 'HIGH': return 'priority-high';
      case 'MEDIUM': return 'priority-medium';
      default: return 'priority-low';
    }
  }
}
