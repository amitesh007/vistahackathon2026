import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: false,
  templateUrl: './stat-card.component.html',
  styleUrls: ['./stat-card.component.scss']
})
export class StatCardComponent {
  @Input() title: string = '';
  @Input() value: string | number = '';
  @Input() icon: string = '';
  @Input() trend: 'up' | 'down' | 'neutral' = 'neutral';
  @Input() trendValue: string = '';
  @Input() color: string = 'primary';
}
