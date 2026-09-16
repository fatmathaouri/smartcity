import { Component } from '@angular/core';
import { ChatbotService } from '../../../core/services/chatbot.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-chatbot-widget',
  templateUrl: './chatbot-widget.component.html',
  styleUrls: ['./chatbot-widget.component.css']
})
export class ChatbotWidgetComponent {
  isOpen = false;
  message = '';
  messages: { text: string; isUser: boolean; time: Date }[] = [];
  loading = false;

  constructor(
    private chatbotService: ChatbotService,
    public authService: AuthService
  ) {
    this.messages.push({
      text: 'Bonjour ! 👋 Je suis l\'assistant SmartCity. Posez-moi vos questions sur les signalements, les catégories, ou la plateforme.',
      isUser: false,
      time: new Date()
    });
  }

  toggle(): void {
    this.isOpen = !this.isOpen;
  }

  send(): void {
    if (!this.message.trim() || this.loading) return;
    const userMsg = this.message.trim();
    this.messages.push({ text: userMsg, isUser: true, time: new Date() });
    this.message = '';
    this.loading = true;

    this.chatbotService.chat(userMsg).subscribe({
      next: (res) => {
        this.messages.push({ text: res.reply, isUser: false, time: new Date() });
        this.loading = false;
      },
      error: () => {
        this.messages.push({ text: 'Désolé, une erreur est survenue. Réessayez.', isUser: false, time: new Date() });
        this.loading = false;
      }
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }
}
