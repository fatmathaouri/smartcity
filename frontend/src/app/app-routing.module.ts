import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { ChangePasswordComponent } from './features/auth/change-password/change-password.component';
import { ReportListComponent } from './features/reports/report-list/report-list.component';
import { ReportCreateComponent } from './features/reports/report-create/report-create.component';
import { ReportDetailComponent } from './features/reports/report-detail/report-detail.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MapViewComponent } from './features/map/map-view/map-view.component';
import { NotificationListComponent } from './features/notifications/notification-list/notification-list.component';
import { HomeComponent } from './features/home/home.component';
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard.component';
import { AgentDashboardComponent } from './features/agent/agent-dashboard/agent-dashboard.component';
import { ManagerDashboardComponent } from './features/manager/manager-dashboard/manager-dashboard.component';
import { PlanningCalendarComponent } from './features/planning/planning-calendar/planning-calendar.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'change-password', component: ChangePasswordComponent, canActivate: [AuthGuard] },
  { path: 'reports', component: ReportListComponent, canActivate: [AuthGuard] },
  { path: 'reports/new', component: ReportCreateComponent, canActivate: [AuthGuard] },
  { path: 'reports/:id', component: ReportDetailComponent, canActivate: [AuthGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'map', component: MapViewComponent, canActivate: [AuthGuard] },
  { path: 'notifications', component: NotificationListComponent, canActivate: [AuthGuard] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_ADMIN'] } },
  { path: 'agent', component: AgentDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_MUNICIPAL_AGENT'] } },
  { path: 'manager', component: ManagerDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_DEPARTMENT_MANAGER'] } },
  { path: 'planning', component: PlanningCalendarComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
