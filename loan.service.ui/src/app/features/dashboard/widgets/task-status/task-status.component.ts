import { Component, OnInit } from '@angular/core';

interface TaskStatus {
  name: string;
  color: string;
  count: number;
  percentage: number;
}

interface TaskType {
  name: string;
  completed: number;
  pending: number;
  wip: number;
}

@Component({
  selector: 'app-task-status',
  standalone: false,
  templateUrl: './task-status.component.html',
  styleUrls: ['./task-status.component.scss']
})
export class TaskStatusComponent implements OnInit {
  taskStatuses: TaskStatus[] = [];
  taskTypes: TaskType[] = [];

  ngOnInit(): void {
    this.loadTaskStatus();
  }

  loadTaskStatus(): void {
    this.taskStatuses = [
      { name: 'Customer Creation', color: '#694ED6', count: 166, percentage: 65 },
      { name: 'Modification', color: '#FF6B9D', count: 33, percentage: 13 },
      { name: 'Approvals', color: '#00C9A7', count: 43, percentage: 17 },
      { name: 'Others', color: '#FFB946', count: 21, percentage: 8 }
    ];

    this.taskTypes = [
      { name: 'Customer Creation', completed: 106, pending: 32, wip: 28 },
      { name: 'Modification', completed: 18, pending: 10, wip: 5 },
      { name: 'Approvals', completed: 25, pending: 12, wip: 8 },
      { name: 'Others', completed: 12, pending: 6, wip: 3 }
    ];
  }
}
