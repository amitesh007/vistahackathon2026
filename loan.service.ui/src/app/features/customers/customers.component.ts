import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-customers',
  standalone: false,
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Customers</h1>
          <p class="page-subtitle">Manage your customer accounts</p>
        </div>
        <button class="create-btn" (click)="openCreateForm()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Create Customer
        </button>
      </div>

      <div class="empty-state">
        <div class="empty-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
        </div>
        <p class="empty-text">No customers yet</p>
        <p class="empty-subtext">Click <strong>Create Customer</strong> to add your first customer.</p>
      </div>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }

    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 32px;
      padding: 24px;
      background: var(--bg-primary);
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-sm);
    }

    .page-title {
      font-size: 22px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .page-subtitle {
      font-size: 13px;
      color: var(--text-secondary);
    }

    .create-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 10px 20px;
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      color: #fff;
      border: none;
      border-radius: var(--radius-md);
      font-size: 13px;
      font-weight: 500;
      cursor: pointer;
      box-shadow: var(--shadow-sm);
      transition: all 0.2s ease;

      &:hover {
        box-shadow: var(--shadow-md);
        transform: translateY(-1px);
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
      background: var(--bg-primary);
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-sm);
      text-align: center;
    }

    .empty-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      background: var(--bg-secondary);
      border-radius: 50%;
      color: var(--text-tertiary);
      margin-bottom: 16px;
    }

    .empty-text {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    .empty-subtext {
      font-size: 13px;
      color: var(--text-secondary);
    }
  `]
})
export class CustomersComponent {
  constructor(private router: Router) {}

  openCreateForm(): void {
    this.router.navigate(['/customers/create']);
  }
}
