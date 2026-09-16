import { Component, OnInit } from '@angular/core';
import { ReportService } from '../../../core/services/report.service';
import { Report } from '../../../core/models/report.model';

@Component({
  selector: 'app-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.css']
})
export class MapViewComponent implements OnInit {
  reports: Report[] = [];
  mapUrl = '';

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.reportService.getAll().subscribe(data => {
      this.reports = data.filter(r => r.latitude && r.longitude);
      this.updateMap();
    });
  }

  updateMap(): void {
    if (this.reports.length === 0) return;
    const markers = this.reports.map(r =>
      `markers=color:red%7C${r.latitude},${r.longitude}`
    ).join('&');
    this.mapUrl = `https://www.openstreetmap.org/export/embed.html?bbox=-10,30,35,45&layer=mapnik&marker=${this.reports[0].latitude},${this.reports[0].longitude}`;
  }

  getGoogleMapsUrl(): string {
    if (this.reports.length === 0) return '';
    const coords = this.reports.filter(r => r.latitude && r.longitude);
    if (coords.length === 0) return '';
    const center = coords[0];
    return `https://www.google.com/maps/embed/v1/place?key=&q=${center.latitude},${center.longitude}&zoom=12`;
  }
}
