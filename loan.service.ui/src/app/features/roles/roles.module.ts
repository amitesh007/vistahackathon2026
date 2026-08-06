import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { RolesComponent } from './roles.component';

const routes: Routes = [
  {
    path: '',
    component: RolesComponent
  }
];

@NgModule({
  declarations: [RolesComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class RolesModule { }
