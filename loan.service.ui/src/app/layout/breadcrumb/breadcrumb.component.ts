import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter, distinctUntilChanged } from 'rxjs/operators';

export interface Breadcrumb {
  label: string;
  url: string;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: false,
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit {
  breadcrumbs: Breadcrumb[] = [];

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.breadcrumbs = this.buildBreadcrumbs(this.activatedRoute.root);
      });
    
    // Initial breadcrumb
    this.breadcrumbs = this.buildBreadcrumbs(this.activatedRoute.root);
  }

  private buildBreadcrumbs(
    route: ActivatedRoute,
    url: string = '',
    breadcrumbs: Breadcrumb[] = []
  ): Breadcrumb[] {
    // Add Home breadcrumb at the start
    if (breadcrumbs.length === 0) {
      breadcrumbs.push({
        label: 'Home',
        url: '/'
      });
    }

    // Get the child routes
    const children: ActivatedRoute[] = route.children;

    // Return if there are no more children
    if (children.length === 0) {
      return breadcrumbs;
    }

    // Iterate over each child
    for (const child of children) {
      // Verify if the route has a path
      const routeURL: string = child.snapshot.url
        .map(segment => segment.path)
        .join('/');
      
      // If the route is empty, skip it
      if (routeURL !== '') {
        url += `/${routeURL}`;
      }

      // Get the route's label from data
      const label = child.snapshot.data['breadcrumb'];
      
      // Add breadcrumb only if label exists
      if (label) {
        breadcrumbs.push({
          label,
          url
        });
      }

      // Recursive call
      return this.buildBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }

  navigateTo(url: string): void {
    this.router.navigateByUrl(url);
  }
}
