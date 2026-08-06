import { Component } from '@angular/core';

@Component({
  selector: 'app-roles',
  standalone: false,
  template: `
    <div class="page-container">
      <h1>Roles</h1>
      <p>Role management page coming soon...</p>
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
export class RolesComponent {
}
