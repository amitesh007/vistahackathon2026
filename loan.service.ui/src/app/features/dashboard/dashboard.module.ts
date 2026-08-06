import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard.component';
import { StatCardComponent } from './widgets/stat-card/stat-card.component';
import { PendingApprovalsComponent } from './widgets/pending-approvals/pending-approvals.component';
import { CustomersChartComponent } from './widgets/customers-chart/customers-chart.component';
import { TaskStatusComponent } from './widgets/task-status/task-status.component';
import { CalendarWidgetComponent } from './widgets/calendar-widget/calendar-widget.component';
import { WorldMapComponent } from './widgets/world-map/world-map.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent
  }
];

@NgModule({
  declarations: [
    DashboardComponent,
    StatCardComponent,
    PendingApprovalsComponent,
    CustomersChartComponent,
    TaskStatusComponent,
    CalendarWidgetComponent,
    WorldMapComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class DashboardModule { }
