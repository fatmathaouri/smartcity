import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReportService } from '../../../core/services/report.service';
import { CategoryService } from '../../../core/services/category.service';
import { AiService } from '../../../core/services/ai.service';
import { Category, ClassificationResult } from '../../../core/models/report.model';

@Component({
  selector: 'app-report-create',
  templateUrl: './report-create.component.html',
  styleUrls: ['./report-create.component.css']
})
export class ReportCreateComponent implements OnInit {
  report: any = { title: '', description: '', latitude: null, longitude: null, address: '', categoryId: null };
  categories: Category[] = [];
  selectedFiles: File[] = [];
  selectedVideo: File | null = null;
  photoPreviews: string[] = [];
  videoPreview: string | null = null;
  classification: ClassificationResult | null = null;
  isSubmitting = false;

  get selectedCategoryName(): string {
    const cat = this.categories.find(c => c.id === this.report.categoryId);
    return cat ? cat.name : '';
  }

  get showPreview(): boolean {
    return !!(this.report.title || this.report.description || this.report.address);
  }

  constructor(
    private reportService: ReportService,
    private categoryService: CategoryService,
    private aiService: AiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(data => this.categories = data);
  }

  onDescriptionChange(): void {
    if (this.report.description && this.report.description.length > 10) {
      this.aiService.classify(this.report.title + ' ' + this.report.description).subscribe({
        next: (res) => {
          this.classification = res;
          const cat = this.categories.find(c => c.name === res.category);
          if (cat) this.report.categoryId = cat.id;
        }
      });
    }
  }

  onFilesSelected(event: any): void {
    const files: FileList = event.target.files;
    for (let i = 0; i < files.length && this.selectedFiles.length < 5; i++) {
      const file = files[i];
      if (file.type.startsWith('image/') && file.size <= 10 * 1024 * 1024) {
        this.selectedFiles.push(file);
        const reader = new FileReader();
        reader.onload = (e) => this.photoPreviews.push(e.target?.result as string);
        reader.readAsDataURL(file);
      }
    }
  }

  onVideoSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file && (file.type === 'video/mp4' || file.type === 'video/webm') && file.size <= 30 * 1024 * 1024) {
      this.selectedVideo = file;
      const reader = new FileReader();
      reader.onload = (e) => this.videoPreview = e.target?.result as string;
      reader.readAsDataURL(file);
    }
  }

  removePhoto(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.photoPreviews.splice(index, 1);
  }

  removeVideo(): void {
    this.selectedVideo = null;
    this.videoPreview = null;
  }

  onLocationSelected(location: { lat: number; lng: number; address: string }): void {
    this.report.latitude = location.lat;
    this.report.longitude = location.lng;
    this.report.address = location.address;
  }

  createReport(): void {
    this.isSubmitting = true;
    this.reportService.create(this.report).subscribe({
      next: (report) => {
        const uploads: Promise<any>[] = [];
        this.selectedFiles.forEach(file => {
          uploads.push(this.reportService.addPhoto(report.id, file).toPromise());
        });
        if (this.selectedVideo) {
          uploads.push(this.reportService.addVideo(report.id, this.selectedVideo).toPromise());
        }
        Promise.all(uploads).then(() => {
          this.isSubmitting = false;
          this.router.navigate(['/reports', report.id]);
        }).catch(() => {
          this.isSubmitting = false;
          this.router.navigate(['/reports', report.id]);
        });
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }
}
