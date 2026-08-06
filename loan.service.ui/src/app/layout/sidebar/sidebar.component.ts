import { Component, Input } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  active?: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  @Input() collapsed = false;

  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
      active: true
    },
    {
      label: 'Customers',
      icon: 'customers',
      route: '/customers',
      active: false
    },
    {
      label: 'Roles',
      icon: 'roles',
      route: '/roles',
      active: false
    },
    {
      label: 'Templates',
      icon: 'templates',
      route: '/templates',
      active: false
    }
  ];

  constructor(private router: Router) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateActiveMenuItem();
      });
  }

  private updateActiveMenuItem(): void {
    const currentRoute = this.router.url;
    this.menuItems.forEach(item => {
      item.active = currentRoute.startsWith(item.route);
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}
