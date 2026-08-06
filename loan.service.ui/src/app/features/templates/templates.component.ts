import { Component } from '@angular/core';

@Component({
  selector: 'app-templates',
  standalone: false,
  template: `
    <div class="page-container">
      <h1>Templates</h1>
      <p>Template management page coming soon...</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }
    
    h1 {
      font-size: 28px;
      margin-bottom: 16px;
      color: var(--text-primary);
    }
    
    p {
      color: var(--text-secondary);
    }
  `]
})
export class TemplatesComponent {
}
