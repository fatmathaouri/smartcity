import { Component, OnInit, OnDestroy, AfterViewInit, Output, EventEmitter, Input, Inject, PLATFORM_ID, ViewEncapsulation } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Subject, debounceTime, switchMap } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-map-picker',
  templateUrl: './map-picker.component.html',
  styleUrls: ['./map-picker.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class MapPickerComponent implements AfterViewInit, OnDestroy {
  @Input() initialLat = 36.8065;
  @Input() initialLng = 10.1815;
  @Output() locationSelected = new EventEmitter<{ lat: number; lng: number; address: string }>();

  address = '';
  latitude: number | null = null;
  longitude: number | null = null;

  private map: any;
  private marker: any;
  private addressSearch$ = new Subject<string>();
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngAfterViewInit(): void {
    if (this.isBrowser) {
      setTimeout(() => this.initMap(), 150);
      this.setupAddressSearch();
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  private async initMap(): Promise<void> {
    const L = await import('leaflet');

    this.map = L.map('map-picker', {
      center: [this.initialLat, this.initialLng],
      zoom: 13,
      zoomControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);

    const defaultIcon = L.divIcon({
      className: 'custom-marker',
      html: '<div class="marker-pin"></div>',
      iconSize: [30, 42],
      iconAnchor: [15, 42]
    });

    this.marker = L.marker([this.initialLat, this.initialLng], {
      icon: defaultIcon,
      draggable: true
    }).addTo(this.map);

    this.marker.on('dragend', () => {
      const pos = this.marker.getLatLng();
      this.latitude = Math.round(pos.lat * 100000) / 100000;
      this.longitude = Math.round(pos.lng * 100000) / 100000;
      this.reverseGeocode(this.latitude, this.longitude);
    });

    this.map.on('click', (e: any) => {
      const { lat, lng } = e.latlng;
      this.latitude = Math.round(lat * 100000) / 100000;
      this.longitude = Math.round(lng * 100000) / 100000;
      this.marker.setLatLng([lat, lng]);
      this.reverseGeocode(this.latitude, this.longitude);
    });

    setTimeout(() => this.map.invalidateSize(), 200);
  }

  private setupAddressSearch(): void {
    this.addressSearch$.pipe(
      debounceTime(600),
      switchMap(query => this.geocode(query))
    ).subscribe(results => {
      if (results.length > 0) {
        const first = results[0];
        this.latitude = parseFloat(first.lat);
        this.longitude = parseFloat(first.lon);
        this.marker.setLatLng([this.latitude, this.longitude]);
        this.map.setView([this.latitude, this.longitude], 15);
        this.emitLocation(first.display_name);
      }
    });
  }

  onAddressInput(value: string): void {
    this.address = value;
    if (value && value.length > 3) {
      this.addressSearch$.next(value);
    }
  }

  private geocode(query: string): Promise<any[]> {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&limit=5&countrycodes=tn`;
    return this.http.get<any[]>(url).toPromise().then(res => res || []);
  }

  private reverseGeocode(lat: number, lng: number): void {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=fr`;
    this.http.get<any>(url).subscribe(data => {
      if (data && data.display_name) {
        const short = this.shortenAddress(data.display_name);
        this.address = short;
        this.emitLocation(short);
      }
    });
  }

  private shortenAddress(full: string): string {
    const parts = full.split(',');
    if (parts.length >= 3) {
      return parts.slice(0, 3).join(',').trim();
    }
    return full;
  }

  private emitLocation(addr: string): void {
    this.locationSelected.emit({
      lat: this.latitude!,
      lng: this.longitude!,
      address: addr
    });
  }
}
