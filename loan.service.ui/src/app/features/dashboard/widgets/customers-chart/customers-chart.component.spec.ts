import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { CustomersChartComponent } from './customers-chart.component';

describe('CustomersChartComponent', () => {
  let component: CustomersChartComponent;
  let fixture: ComponentFixture<CustomersChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [ CustomersChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomersChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
