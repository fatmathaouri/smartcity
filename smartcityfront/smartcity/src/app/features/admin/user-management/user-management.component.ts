import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../../core/services/admin.service';
import { Department, CreateUserRequest, UserCreationResult } from '../../../core/models/user.model';
import { TempPasswordDialogComponent } from '../../../shared/components/temp-password-dialog/temp-password-dialog.component';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  filteredUsers: any[] = [];
  departments: Department[] = [];
  departmentsManagerStatus: any[] = [];
  occupiedDepartmentIds: Set<number> = new Set();
  search = '';
  newRole = '';
  selectedUser: any = null;
  roles = ['ROLE_CITIZEN', 'ROLE_ADMIN', 'ROLE_MUNICIPALITY', 'ROLE_MUNICIPAL_AGENT', 'ROLE_DEPARTMENT_MANAGER'];

  showCreateForm = false;
  newUser: CreateUserRequest = {
    email: '', firstName: '', lastName: '', phone: '', roleName: '', departmentId: 0
  };
  reassignDepartmentId: number = 0;

  constructor(
    private adminService: AdminService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadDepartments();
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe(u => {
      this.users = u;
      this.filteredUsers = u;
    });
  }

  loadDepartments(): void {
    this.adminService.getDepartmentsManagerStatus().subscribe(data => {
      this.departmentsManagerStatus = data;
      this.occupiedDepartmentIds = new Set(data.filter(d => d.hasManager).map(d => d.id));
    });
  }

  isDepartmentOccupied(deptId: number): boolean {
    return this.occupiedDepartmentIds.has(deptId);
  }

  filterUsers(): void {
    const s = this.search.toLowerCase();
    this.filteredUsers = this.users.filter(u =>
      u.email.toLowerCase().includes(s) ||
      u.firstName?.toLowerCase().includes(s) ||
      u.lastName?.toLowerCase().includes(s)
    );
  }

  selectUser(user: any): void {
    this.selectedUser = { ...user, roles: [...user.roles] };
    this.reassignDepartmentId = 0;
  }

  reassignManager(): void {
    if (!this.selectedUser || !this.reassignDepartmentId) return;
    this.adminService.reassignManager(this.selectedUser.id, this.reassignDepartmentId).subscribe({
      next: (result) => {
        let msg = `${result.userName} réassigné au département ${result.departmentName}`;
        if (result.swapped) {
          msg += ` (échange avec ${result.swappedWith})`;
        }
        this.snackBar.open(msg, 'Fermer', { duration: 4000 });
        this.reassignDepartmentId = 0;
        this.loadUsers();
        this.loadDepartments();
        this.selectedUser = null;
      },
      error: (err) => this.snackBar.open(err.error?.error || 'Erreur lors de la réassignation', 'Fermer', { duration: 3000 })
    });
  }

  toggleEnabled(user: any): void {
    this.adminService.toggleUserEnabled(user.id).subscribe(updated => {
      const idx = this.users.findIndex(u => u.id === user.id);
      if (idx >= 0) this.users[idx] = updated;
      this.filterUsers();
      this.snackBar.open('Utilisateur mis à jour', 'Fermer', { duration: 2000 });
    });
  }

  addRole(role: string): void {
    if (!this.selectedUser || !role) return;
    this.adminService.updateUserRole(this.selectedUser.id, role).subscribe(updated => {
      this.selectedUser = updated;
      const idx = this.users.findIndex(u => u.id === updated.id);
      if (idx >= 0) this.users[idx] = updated;
      this.filterUsers();
    });
  }

  removeRole(role: string): void {
    if (!this.selectedUser) return;
    this.adminService.removeUserRole(this.selectedUser.id, role).subscribe(updated => {
      this.selectedUser = updated;
      const idx = this.users.findIndex(u => u.id === updated.id);
      if (idx >= 0) this.users[idx] = updated;
      this.filterUsers();
    });
  }

  deleteUser(user: any): void {
    if (!confirm(`Supprimer ${user.email} ?`)) return;
    this.adminService.deleteUser(user.id).subscribe(() => {
      this.users = this.users.filter(u => u.id !== user.id);
      this.filterUsers();
      this.selectedUser = null;
      this.snackBar.open('Utilisateur supprimé', 'Fermer', { duration: 2000 });
    });
  }

  createUser(): void {
    if (!this.newUser.email || !this.newUser.firstName || !this.newUser.lastName || !this.newUser.roleName) {
      this.snackBar.open('Veuillez remplir tous les champs obligatoires', 'Fermer', { duration: 3000 });
      return;
    }

    if (['ROLE_MUNICIPAL_AGENT', 'ROLE_DEPARTMENT_MANAGER'].includes(this.newUser.roleName) && !this.newUser.departmentId) {
      this.snackBar.open('Veuillez sélectionner un département', 'Fermer', { duration: 3000 });
      return;
    }

    this.adminService.createUser(this.newUser).subscribe({
      next: (result) => {
        this.dialog.open(TempPasswordDialogComponent, {
          data: result,
          width: '500px',
          disableClose: true
        });
        this.resetCreateForm();
        this.loadUsers();
      },
      error: (err) => this.snackBar.open(err.error?.error || 'Erreur lors de la création', 'Fermer', { duration: 3000 })
    });
  }

  resetTempPassword(userId: number): void {
    this.adminService.resetTempPassword(userId).subscribe({
      next: (result) => {
        this.dialog.open(TempPasswordDialogComponent, {
          data: result,
          width: '500px',
          disableClose: true
        });
      },
      error: (err) => this.snackBar.open(err.error?.error || 'Erreur', 'Fermer', { duration: 3000 })
    });
  }

  resetCreateForm(): void {
    this.newUser = { email: '', firstName: '', lastName: '', phone: '', roleName: '', departmentId: 0 };
    this.showCreateForm = false;
  }

  getRoleLabel(role: string): string {
    const labels: Record<string, string> = {
      'ROLE_CITIZEN': 'Citoyen',
      'ROLE_ADMIN': 'Admin',
      'ROLE_MUNICIPALITY': 'Municipalité',
      'ROLE_MUNICIPAL_AGENT': 'Agent',
      'ROLE_DEPARTMENT_MANAGER': 'Manager'
    };
    return labels[role] || role;
  }

  getRoleClass(role: string): string {
    const classes: Record<string, string> = {
      'ROLE_ADMIN': 'role-admin',
      'ROLE_MUNICIPAL_AGENT': 'role-agent',
      'ROLE_DEPARTMENT_MANAGER': 'role-manager',
      'ROLE_MUNICIPALITY': 'role-municipality',
      'ROLE_CITIZEN': 'role-citizen'
    };
    return classes[role] || '';
  }
}
