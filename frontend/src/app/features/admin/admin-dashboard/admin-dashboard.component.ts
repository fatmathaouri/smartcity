import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../../core/services/admin.service';
import { ReportService } from '../../../core/services/report.service';
import { Department } from '../../../core/models/user.model';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  activeTab = 'stats';
  stats: any = null;
  heatmap: any[] = [];
  maxHeatCount = 1;
  slaStats: any = null;

  departments: Department[] = [];
  newDept = { name: '', description: '', categoryId: 0 };
  categories: any[] = [];

  constructor(
    private adminService: AdminService,
    private reportService: ReportService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.adminService.getGlobalStats().subscribe(s => this.stats = s);
    this.adminService.getHeatmapData().subscribe(h => {
      this.heatmap = h;
      this.maxHeatCount = Math.max(...h.map(z => z.count), 1);
    });
    this.reportService.getSlaStats().subscribe(s => this.slaStats = s);
    this.loadDepartments();
    this.loadCategories();
  }

  loadDepartments(): void {
    this.adminService.getDepartments().subscribe(d => this.departments = d);
  }

  loadCategories(): void {
    this.adminService.getGlobalStats().subscribe(s => this.categories = s.categories || []);
  }

  createDepartment(): void {
    if (!this.newDept.name) {
      this.snackBar.open('Nom requis', 'Fermer', { duration: 2000 });
      return;
    }
    this.adminService.createDepartment(this.newDept).subscribe({
      next: () => {
        this.loadDepartments();
        this.newDept = { name: '', description: '', categoryId: 0 };
        this.snackBar.open('Département créé', 'Fermer', { duration: 2000 });
      },
      error: (err) => this.snackBar.open(err.error?.error || 'Erreur', 'Fermer', { duration: 2000 })
    });
  }

  deleteDepartment(id: number): void {
    if (!confirm('Supprimer ce département ?')) return;
    this.adminService.deleteDepartment(id).subscribe(() => {
      this.loadDepartments();
      this.snackBar.open('Département supprimé', 'Fermer', { duration: 2000 });
    });
  }

  getHeatWidth(count: number): number {
    return Math.round((count / this.maxHeatCount) * 100);
  }

  getHeatColor(count: number): string {
    const ratio = count / this.maxHeatCount;
    if (ratio > 0.7) return '#dc2626';
    if (ratio > 0.4) return '#f59e0b';
    return '#10b981';
  }
}
