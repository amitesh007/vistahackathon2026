import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();
  
  currentUser = {
    name: 'Alexander Steve',
    role: 'Senior Manager',
    avatar: ''
  };

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  onSearch(query: string): void {
    // Implement search functionality
    console.log('Searching for:', query);
  }

  onLogout(): void {
    // Implement logout functionality
    console.log('Logging out...');
  }
}
