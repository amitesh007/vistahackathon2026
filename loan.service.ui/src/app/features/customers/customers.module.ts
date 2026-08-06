import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { CustomersComponent } from './customers.component';
import { CreateCustomerComponent } from './create-customer/create-customer.component';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  {
    path: '',
    component: CustomersComponent
  },
  {
    path: 'create',
    component: CreateCustomerComponent,
    data: { breadcrumb: 'Create Customer' }
  }
];

@NgModule({
  declarations: [CustomersComponent, CreateCustomerComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class CustomersModule { }
