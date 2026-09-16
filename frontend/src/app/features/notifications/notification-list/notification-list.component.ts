import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/report.model';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.css']
})
export class NotificationListComponent implements OnInit {
  notifications: Notification[] = [];
  filter = 'all';

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.notificationService.getAll().subscribe(data => this.notifications = data);
  }

  get filteredNotifications(): Notification[] {
    if (this.filter === 'unread') return this.notifications.filter(n => !n.read);
    if (this.filter === 'read') return this.notifications.filter(n => n.read);
    return this.notifications;
  }

  markAsRead(n: Notification): void {
    if (!n.read) {
      this.notificationService.markAsRead(n.id).subscribe(() => {
        n.read = true;
      });
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.notifications.forEach(n => n.read = true);
    });
  }

  getIcon(type: string): string {
    const icons: Record<string, string> = {
      'REPORT_CREATED': 'add_circle',
      'STATUS_CHANGED': 'autorenew',
      'VOTE_RECEIVED': 'thumb_up',
      'COMMENT_ADDED': 'chat_bubble',
      'BADGE_EARNED': 'emoji_events',
      'PLANNING': 'calendar_month',
      'SLA_WARNING': 'timer',
      'SLA_BREACH': 'warning',
      'INFO': 'info'
    };
    return icons[type] || 'notifications';
  }

  getColor(type: string): string {
    const colors: Record<string, string> = {
      'REPORT_CREATED': '#3b82f6',
      'STATUS_CHANGED': '#f59e0b',
      'VOTE_RECEIVED': '#ec4899',
      'COMMENT_ADDED': '#8b5cf6',
      'BADGE_EARNED': '#f59e0b',
      'PLANNING': '#10b981',
      'SLA_WARNING': '#f97316',
      'SLA_BREACH': '#dc2626',
      'INFO': '#6b7280'
    };
    return colors[type] || '#6b7280';
  }
}
