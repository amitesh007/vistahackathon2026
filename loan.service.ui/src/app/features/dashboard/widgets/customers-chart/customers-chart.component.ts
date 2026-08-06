import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';

@Component({
  selector: 'app-customers-chart',
  standalone: false,
  templateUrl: './customers-chart.component.html',
  styleUrls: ['./customers-chart.component.scss']
})
export class CustomersChartComponent implements OnInit, AfterViewInit {
  @ViewChild('chartCanvas', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  
  selectedYear = '2024';
  chartData = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    datasets: [
      {
        label: 'Corporate Channels',
        data: [50, 65, 55, 70, 60, 75, 65, 80, 70, 85, 75, 70],
        color: '#4E73DF'
      },
      {
        label: 'Loan IQ',
        data: [45, 55, 70, 60, 75, 65, 80, 90, 75, 70, 80, 75],
        color: '#FF6B9D'
      },
      {
        label: 'Trade Innovation',
        data: [55, 60, 45, 65, 55, 70, 60, 75, 85, 90, 70, 80],
        color: '#00C9A7'
      }
    ]
  };

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.renderChart();
  }

  onYearChange(year: string): void {
    this.selectedYear = year;
    // Update chart data based on year
  }

  renderChart(): void {
    const canvas = this.chartCanvas.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;
    const padding = 40;
    const chartWidth = width - 2 * padding;
    const chartHeight = height - 2 * padding;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    // Draw grid lines
    ctx.strokeStyle = '#f0f0f0';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = padding + (chartHeight / 4) * i;
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(width - padding, y);
      ctx.stroke();
    }

    // Draw lines
    this.chartData.datasets.forEach(dataset => {
      ctx.strokeStyle = dataset.color;
      ctx.lineWidth = 2;
      ctx.beginPath();

      dataset.data.forEach((value, index) => {
        const x = padding + (chartWidth / (dataset.data.length - 1)) * index;
        const y = padding + chartHeight - (value / 100) * chartHeight;

        if (index === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
      });

      ctx.stroke();
    });
  }
}
