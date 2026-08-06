import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { PendingApproval, ApprovalListResponse } from '../models/approval.model';

@Injectable({
  providedIn: 'root'
})
export class CommonService {
  private apiUrl = '/api/approvals'; // This will be proxied by nginx

  constructor(private http: HttpClient) {}

  /**
   * Get pending approvals list
   * @param page - Page number
   * @param pageSize - Items per page
   * @param sortBy - Sort field (customer, product, task)
   */
  getPendingApprovals(page: number = 1, pageSize: number = 10, sortBy: string = 'customer'): Observable<ApprovalListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('pageSize', pageSize.toString())
      .set('sortBy', sortBy);

    // For now, return mock data. Replace with actual API call when backend is ready
    return this.getMockApprovals(page, pageSize, sortBy);
    
    // Uncomment this line when the real API is ready:
    // return this.http.get<ApprovalListResponse>(this.apiUrl, { params });
  }

  /**
   * Approve an approval request
   */
  approveRequest(approvalId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${approvalId}/approve`, {});
  }

  /**
   * Reject an approval request
   */
  rejectRequest(approvalId: string, reason?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${approvalId}/reject`, { reason });
  }

  /**
   * Get approval details
   */
  getApprovalDetails(approvalId: string): Observable<PendingApproval> {
    return this.http.get<PendingApproval>(`${this.apiUrl}/${approvalId}`);
  }

  /**
   * Get current date and time
   */
  getCurrentDateTime(): Date {
    return new Date();
  }

  /**
   * Get date N days ago from today
   */
  getDateDaysAgo(days: number): Date {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  }

  /**
   * Format date for display
   */
  formatDate(date: Date, format: 'short' | 'long' = 'long'): string {
    if (format === 'short') {
      return date.toLocaleDateString();
    }
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  /**
   * Format time for display
   */
  formatTime(date: Date, includeSeconds: boolean = true): string {
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      second: includeSeconds ? '2-digit' : undefined,
      hour12: true
    });
  }

  /**
   * Mock data for development/demo purposes
   * This simulates an API call with delay
   */
  private getMockApprovals(page: number, pageSize: number, sortBy: string): Observable<ApprovalListResponse> {
    // Use current date/time for mock data
    const today = this.getCurrentDateTime();
    
    const mockData: PendingApproval[] = [
      {
        id: '1',
        customer: {
          name: 'Lorem Ipsum',
          initials: 'LI'
        },
        products: ['FCC', 'Trade Innovation'],
        task: 'Creation',
        createdDate: this.getDateDaysAgo(1), // Yesterday
        status: 'pending'
      },
      {
        id: '2',
        customer: {
          name: 'Lorem Ipsum',
          initials: 'LI'
        },
        products: ['Loan IQ'],
        task: 'Amendment',
        createdDate: this.getDateDaysAgo(2), // 2 days ago
        status: 'pending'
      },
      {
        id: '3',
        customer: {
          name: 'Lorem Ipsum',
          initials: 'LI'
        },
        products: ['FCC', 'Loan IQ'],
        task: 'Creation',
        createdDate: this.getDateDaysAgo(3), // 3 days ago
        status: 'pending'
      },
      {
        id: '4',
        customer: {
          name: 'Lorem Ipsum',
          initials: 'LI'
        },
        products: ['FCC'],
        task: 'Amendment',
        createdDate: this.getDateDaysAgo(4), // 4 days ago
        status: 'pending'
      }
    ];

    // Simulate sorting
    const sortedData = [...mockData].sort((a, b) => {
      if (sortBy === 'customer') {
        return a.customer.name.localeCompare(b.customer.name);
      } else if (sortBy === 'product') {
        return a.products[0].localeCompare(b.products[0]);
      } else if (sortBy === 'task') {
        return a.task.localeCompare(b.task);
      }
      return 0;
    });

    // Simulate pagination
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedData = sortedData.slice(startIndex, endIndex);

    const response: ApprovalListResponse = {
      approvals: paginatedData,
      total: mockData.length,
      page,
      pageSize
    };

    // Simulate network delay
    return of(response).pipe(delay(500));
  }
}
