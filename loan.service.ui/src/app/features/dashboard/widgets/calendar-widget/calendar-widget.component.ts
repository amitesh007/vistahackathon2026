import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-calendar-widget',
  standalone: false,
  templateUrl: './calendar-widget.component.html',
  styleUrls: ['./calendar-widget.component.scss']
})
export class CalendarWidgetComponent implements OnInit {
  currentDate = new Date();
  currentMonth: string = '';
  currentYear: number = 0;
  weekDays = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
  calendarDays: Array<number | null> = [];
  selectedDay = 4;

  ngOnInit(): void {
    this.updateCalendar();
  }

  updateCalendar(): void {
    this.currentMonth = this.currentDate.toLocaleString('default', { month: 'long' });
    this.currentYear = this.currentDate.getFullYear();
    this.generateCalendarDays();
  }

  generateCalendarDays(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    
    this.calendarDays = [];
    
    // Add empty cells for days before the first day of month
    const offset = firstDay === 0 ? 6 : firstDay - 1;
    for (let i = 0; i < offset; i++) {
      this.calendarDays.push(null);
    }
    
    // Add days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      this.calendarDays.push(day);
    }
  }

  previousMonth(): void {
    this.currentDate.setMonth(this.currentDate.getMonth() - 1);
    this.updateCalendar();
  }

  nextMonth(): void {
    this.currentDate.setMonth(this.currentDate.getMonth() + 1);
    this.updateCalendar();
  }

  selectDay(day: number | null): void {
    if (day) {
      this.selectedDay = day;
    }
  }

  isToday(day: number | null): boolean {
    if (!day) return false;
    const today = new Date();
    return day === today.getDate() && 
           this.currentDate.getMonth() === today.getMonth() && 
           this.currentDate.getFullYear() === today.getFullYear();
  }
}
