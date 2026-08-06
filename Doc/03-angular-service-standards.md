ANGULAR SERVICE STANDARDS
==========================

## Injectable Pattern
@Injectable({ providedIn: 'root' })  — default for most services
@Injectable()  — component-scoped (add to component providers array)

## Class Structure Order
1. Injected dependencies (private readonly, via constructor inject)
2. Private state (BehaviorSubjects)
3. Public observables (exposed from private subjects as .asObservable())
4. Private constants (baseUrl, API endpoints)
5. Public CRUD methods
6. Public utility methods (date/time, formatting)
7. Private helpers (handleError, mock data generators)

## Observable Pattern
- Always return Observable from service methods (never subscribe inside service)
- Use RxJS pipe: tap (side effects), catchError (error transform), finalize (cleanup)
- Expose state as readonly streams: readonly data$ = this._data.asObservable()

## HTTP Integration
NgModule apps: import HttpClientModule in AppModule
Pattern for GET list:
  getAll(): Observable<Item[]> {
    return this.http.get<Item[]>(this.baseUrl);
  }
Pattern for POST create:
  create(payload: CreateRequest): Observable<Item> {
    return this.http.post<Item>(this.baseUrl, payload);
  }
Always type responses with interfaces, never use `any`.

## Angular API Integration
- Environment files: src/environments/environment.ts and environment.prod.ts
- Base URL pattern: private readonly baseUrl = environment.apiBaseUrl + '/endpoint'
- Error handling: catchError that returns EMPTY or throwError with user-friendly message
- Loading state: private readonly _isLoading = new BehaviorSubject<boolean>(false)

## CommonService Pattern
**Location:** `src/app/core/services/common.service.ts`

A shared service that provides:
1. Business domain APIs (approvals, workflows)
2. Common utility methods (date/time formatting)
3. Mock data generation for development

### Example Implementation
```typescript
@Injectable({
  providedIn: 'root'
})
export class CommonService {
  private apiUrl = '/api/approvals'; // Proxied by nginx

  constructor(private http: HttpClient) {}

  // Domain-specific API methods
  getPendingApprovals(page: number, pageSize: number, sortBy: string): Observable<ApprovalListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('pageSize', pageSize.toString())
      .set('sortBy', sortBy);
    return this.http.get<ApprovalListResponse>(this.apiUrl, { params });
  }

  approveRequest(approvalId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${approvalId}/approve`, {});
  }

  // Utility methods
  getCurrentDateTime(): Date {
    return new Date();
  }

  getDateDaysAgo(days: number): Date {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  }

  formatDate(date: Date, format: 'short' | 'long' = 'long'): string {
    if (format === 'short') {
      return date.toLocaleDateString();
    }
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' 
    });
  }

  formatTime(date: Date, includeSeconds: boolean = true): string {
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric', minute: '2-digit',
      second: includeSeconds ? '2-digit' : undefined,
      hour12: true
    });
  }
}
```

## Type Safety with Models
**Location:** `src/app/core/models/`

Define TypeScript interfaces for all API request/response types:
```typescript
export interface PendingApproval {
  id: string;
  customer: {
    name: string;
    avatar?: string;
    initials: string;
  };
  products: string[];
  task: 'Creation' | 'Amendment';
  createdDate: Date;
  status: 'pending' | 'approved' | 'rejected';
}

export interface ApprovalListResponse {
  approvals: PendingApproval[];
  total: number;
  page: number;
  pageSize: number;
}
```
