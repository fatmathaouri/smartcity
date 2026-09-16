import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  features = [
    { icon: 'report_problem', title: 'Signalement facile', desc: 'Signalez un problème en quelques clics avec photo et localisation précise.' },
    { icon: 'auto_awesome', title: 'Classification IA', desc: 'Notre IA classe automatiquement votre signalement et définit sa priorité.' },
    { icon: 'map', title: 'Carte interactive', desc: 'Visualisez tous les signalements sur une carte de la ville en temps réel.' },
    { icon: 'dashboard', title: 'Dashboard municipal', desc: 'Suivi en temps réel des statistiques et des tendances par zone.' },
    { icon: 'notifications_active', title: 'Notifications', desc: 'Restez informé de l\'avancement de vos signalements.' },
    { icon: 'trending_up', title: 'Prédictions IA', desc: 'Anticipez les zones à risque grâce à l\'analyse prédictive.' }
  ];

  stats = [
    { value: '500+', label: 'Signalements traités' },
    { value: '98%', label: 'Satisfaction citoyens' },
    { value: '24h', label: 'Délai moyen de réponse' },
    { value: '15', label: 'Quartiers couverts' }
  ];

  constructor(
    private router: Router,
    public authService: AuthService
  ) {}

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}
