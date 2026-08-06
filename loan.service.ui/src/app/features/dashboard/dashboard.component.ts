import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  userName = 'Alexander Steve';
  lastLogin = 'Monday, 3 February 2025 (GMT)';
  currentDateTime = new Date();

  ngOnInit(): void {
    // Update time every second
    setInterval(() => {
      this.currentDateTime = new Date();
    }, 1000);
  }
}
