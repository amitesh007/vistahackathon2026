import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CommonService } from '../../../../core/services/common.service';
import { PendingApproval } from '../../../../core/models/approval.model';

@Component({
  selector: 'app-pending-approvals',
  standalone: false,
  templateUrl: './pending-approvals.component.html',
  styleUrls: ['./pending-approvals.component.scss']
})
export class PendingApprovalsComponent implements OnInit, OnDestroy {
  approvals: PendingApproval[] = [];
  loading = false;
  error: string | null = null;
  sortBy: string = 'customer';
  
  private destroy$ = new Subject<void>();

  constructor(
    private commonService: CommonService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadApprovals();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadApprovals(): void {
    this.loading = true;
    this.error = null;

    this.commonService.getPendingApprovals(1, 10, this.sortBy)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.approvals = response.approvals;
          this.loading = false;
          this.cdr.detectChanges(); // Manually trigger change detection
          console.log('Loaded approvals:', response);
        },
        error: (err) => {
          this.error = 'Failed to load approvals';
          this.loading = false;
          this.cdr.detectChanges(); // Manually trigger change detection
          console.error('Error loading approvals:', err);
        }
      });
  }

  onSortChange(sortBy: string): void {
    this.sortBy = sortBy;
    this.loadApprovals();
  }

  onView(approval: PendingApproval): void {
    console.log('View approval:', approval);
    // TODO: Navigate to approval details or open modal
  }

  onApprove(approval: PendingApproval): void {
    if (confirm(`Approve ${approval.task.toLowerCase()} for ${approval.customer.name}?`)) {
      this.commonService.approveRequest(approval.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            console.log('Approved:', approval);
            this.loadApprovals(); // Reload the list
          },
          error: (err) => {
            console.error('Error approving:', err);
            alert('Failed to approve request');
          }
        });
    }
  }

  onReject(approval: PendingApproval): void {
    const reason = prompt(`Reason for rejecting ${approval.customer.name}'s ${approval.task.toLowerCase()}:`);
    if (reason) {
      this.commonService.rejectRequest(approval.id, reason)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            console.log('Rejected:', approval);
            this.loadApprovals(); // Reload the list
          },
          error: (err) => {
            console.error('Error rejecting:', err);
            alert('Failed to reject request');
          }
        });
    }
  }

  getProductsDisplay(products: string[]): string {
    return products.join(', ');
  }
}

