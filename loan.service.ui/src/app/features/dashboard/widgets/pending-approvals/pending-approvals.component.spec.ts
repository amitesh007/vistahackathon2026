import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PendingApprovalsComponent } from './pending-approvals.component';
import { CommonService } from '../../../../core/services/common.service';

describe('PendingApprovalsComponent', () => {
  let component: PendingApprovalsComponent;
  let fixture: ComponentFixture<PendingApprovalsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientTestingModule],
      declarations: [ PendingApprovalsComponent ],
      providers: [ CommonService ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PendingApprovalsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
