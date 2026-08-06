import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SuccessComponent } from './shared/success/success.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule),
    data: { breadcrumb: 'Dashboard' }
  },
  {
    path: 'customers',
    loadChildren: () => import('./features/customers/customers.module').then(m => m.CustomersModule),
    data: { breadcrumb: 'Customers' }
  },
  {
    path: 'roles',
    loadChildren: () => import('./features/roles/roles.module').then(m => m.RolesModule),
    data: { breadcrumb: 'Roles' }
  },
  {
    path: 'templates',
    loadChildren: () => import('./features/templates/templates.module').then(m => m.TemplatesModule),
    data: { breadcrumb: 'Templates' }
  },
  {
    path: 'success',
    component: SuccessComponent,
    data: { breadcrumb: 'Success' }
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    useHash: false,
    enableTracing: false,
    onSameUrlNavigation: 'reload'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
