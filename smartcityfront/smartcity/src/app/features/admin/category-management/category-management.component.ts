import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { CategoryPublicService } from '../../../core/services/category-public.service';

@Component({
  selector: 'app-category-management',
  templateUrl: './category-management.component.html',
  styleUrls: ['./category-management.component.css']
})
export class CategoryManagementComponent implements OnInit {
  categories: any[] = [];
  showForm = false;
  editingId: number | null = null;
  form = { name: '', description: '', priority: 'MEDIUM', slaHours: 168, escalationHours: 72 };
  priorities = ['LOW', 'MEDIUM', 'HIGH'];

  constructor(
    private adminService: AdminService,
    private categoryService: CategoryPublicService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe(data => this.categories = data);
  }

  openForm(cat?: any): void {
    this.showForm = true;
    if (cat) {
      this.editingId = cat.id;
      this.form = { name: cat.name, description: cat.description, priority: cat.defaultPriority || 'MEDIUM', slaHours: cat.slaHours || 168, escalationHours: cat.escalationHours || 72 };
    } else {
      this.editingId = null;
      this.form = { name: '', description: '', priority: 'MEDIUM', slaHours: 168, escalationHours: 72 };
    }
  }

  save(): void {
    if (this.editingId) {
      this.adminService.updateCategory(this.editingId, this.form).subscribe(() => {
        this.loadCategories();
        this.showForm = false;
      });
    } else {
      this.adminService.createCategory(this.form).subscribe(() => {
        this.loadCategories();
        this.showForm = false;
      });
    }
  }

  deleteCategory(id: number): void {
    if (!confirm('Supprimer cette catégorie ?')) return;
    this.adminService.deleteCategory(id).subscribe(() => this.loadCategories());
  }

  cancel(): void {
    this.showForm = false;
    this.editingId = null;
  }

  getPriorityLabel(p: string): string {
    const l: Record<string, string> = { 'LOW': 'Basse', 'MEDIUM': 'Moyenne', 'HIGH': 'Haute' };
    return l[p] || p;
  }

  getPriorityClass(p: string): string {
    const c: Record<string, string> = { 'LOW': 'priority-low', 'MEDIUM': 'priority-medium', 'HIGH': 'priority-high' };
    return c[p] || '';
  }
}
