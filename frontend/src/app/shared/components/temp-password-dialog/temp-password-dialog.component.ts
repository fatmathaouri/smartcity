import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UserCreationResult } from '../../../core/models/user.model';

@Component({
  selector: 'app-temp-password-dialog',
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="primary">person_add</mat-icon>
      Utilisateur créé avec succès
    </h2>
    <mat-dialog-content>
      <div class="info-row">
        <span class="label">Nom :</span>
        <span class="value">{{ data.username }}</span>
      </div>
      <div class="info-row">
        <span class="label">Email :</span>
        <span class="value">{{ data.email }}</span>
      </div>
      <div class="info-row">
        <span class="label">Rôle :</span>
        <span class="value">{{ data.role }}</span>
      </div>
      <div *ngIf="data.departmentName" class="info-row">
        <span class="label">Département :</span>
        <span class="value">{{ data.departmentName }}</span>
      </div>
      <mat-divider></mat-divider>
      <div class="temp-password-box">
        <mat-icon>vpn_key</mat-icon>
        <div>
          <strong>Mot de passe temporaire :</strong>
          <code class="temp-password">{{ data.temporaryPassword }}</code>
        </div>
      </div>
      <p class="warning">
        <mat-icon>warning</mat-icon>
        Ce mot de passe ne sera affiché qu'une seule fois. L'utilisateur devra le changer à sa première connexion.
      </p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-raised-button color="primary" (click)="close()">
        <mat-icon>check</mat-icon> Fermer
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 0.4rem 0;
      font-size: 0.95rem;
    }
    .label { color: #666; font-weight: 500; }
    .value { color: #1e3a5f; font-weight: 600; }
    .temp-password-box {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      background: #e3f2fd;
      padding: 1rem;
      border-radius: 8px;
      margin-top: 1rem;
    }
    .temp-password {
      display: block;
      font-size: 1.2rem;
      color: #d32f2f;
      font-weight: bold;
      margin-top: 0.25rem;
      word-break: break-all;
    }
    .warning {
      display: flex;
      align-items: flex-start;
      gap: 0.5rem;
      background: #fff3e0;
      padding: 0.75rem;
      border-radius: 8px;
      margin-top: 1rem;
      font-size: 0.85rem;
      color: #e65100;
    }
    .warning mat-icon { color: #e65100; font-size: 1.2rem; width: 1.2rem; height: 1.2rem; }
  `]
})
export class TempPasswordDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<TempPasswordDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserCreationResult
  ) {}

  close(): void {
    this.dialogRef.close();
  }
}
