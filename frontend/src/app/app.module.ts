import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { SafeUrlPipe } from './shared/safe-url.pipe';

import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { ChatbotWidgetComponent } from './shared/components/chatbot-widget/chatbot-widget.component';
import { TempPasswordDialogComponent } from './shared/components/temp-password-dialog/temp-password-dialog.component';
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
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { CategoryManagementComponent } from './features/admin/category-management/category-management.component';
import { AgentDashboardComponent } from './features/agent/agent-dashboard/agent-dashboard.component';
import { ManagerDashboardComponent } from './features/manager/manager-dashboard/manager-dashboard.component';
import { PlanningCalendarComponent } from './features/planning/planning-calendar/planning-calendar.component';
import { MapPickerComponent } from './shared/components/map-picker/map-picker.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    ChatbotWidgetComponent,
    TempPasswordDialogComponent,
    LoginComponent,
    RegisterComponent,
    ChangePasswordComponent,
    ReportListComponent,
    ReportCreateComponent,
    ReportDetailComponent,
    DashboardComponent,
    MapViewComponent,
    NotificationListComponent,
    HomeComponent,
    AdminDashboardComponent,
    UserManagementComponent,
    CategoryManagementComponent,
    AgentDashboardComponent,
    ManagerDashboardComponent,
    PlanningCalendarComponent,
    MapPickerComponent,
    SafeUrlPipe
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    CommonModule,
    AppRoutingModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatSelectModule,
    MatListModule,
    MatBadgeModule,
    MatTabsModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
