ANGULAR UI ARCHITECTURE — loan.service.ui
=========================================

## Technology Stack
- Angular 21.0.0
- TypeScript 5.9.2
- RxJS 7.8.0
- SCSS styling
- NgModule-based architecture (non-standalone)

## Project Structure

```
src/app/
├── app.module.ts                    Root module
├── app-routing.module.ts            Root routing config
├── app.component.ts                 Root component
├── core/                            Core services & models
│   ├── core.module.ts               Core module (singleton services)
│   ├── services/                    Business services
│   │   └── common.service.ts        Shared service with utilities
│   └── models/                      TypeScript interfaces
│       └── approval.model.ts        Data models (e.g., PendingApproval)
├── features/                        Feature modules (lazy-loaded)
│   ├── dashboard/                   Dashboard screen with widgets
│   │   └── widgets/                 Dashboard widgets
│   │       ├── pending-approvals/   Approval list widget
│   │       ├── customers-chart/     Customer charts
│   │       ├── task-status/         Task status widget
│   │       └── calendar-widget/     Calendar component
│   ├── customers/                   Customer list & create form
│   │   └── create-customer/         Complex multi-section form
│   ├── roles/                       Role management
│   └── templates/                   Template management
├── layout/                          Application shell
│   ├── layout.component.ts          Main layout wrapper
│   ├── header/                      Top navigation bar
│   ├── sidebar/                     Left navigation menu
│   └── breadcrumb/                  Breadcrumb navigation
└── shared/                          Reusable utilities
    ├── shared.module.ts             Shared module exports
    └── directives/                  Custom directives
        └── form-field.directive.ts  Input sanitization
```

## Module Architecture

### Root Module (app.module.ts)
```typescript
@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,              // Platform browser support
    BrowserAnimationsModule,    // Angular animations
    HttpClientModule,           // HTTP client for API calls
    AppRoutingModule,           // Root routing
    LayoutModule                // Application shell
  ],
  providers: [],                // App-level services (if any)
  bootstrap: [AppComponent]
})
export class AppModule { }
```

### Core Module Pattern
**Purpose:** Singleton services used across the entire application

**Location:** `src/app/core/`

**core.module.ts:**
```typescript
@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers: [
    // Services are now provided in root via @Injectable({ providedIn: 'root' })
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
```

**Core Module Contents:**
- **services/** — Business logic services (CommonService, AuthService, etc.)
- **models/** — TypeScript interfaces and types
- **guards/** — Route guards (if needed)
- **interceptors/** — HTTP interceptors (if needed)

**CommonService Example:**
```typescript
@Injectable({
  providedIn: 'root'  // Singleton across application
})
export class CommonService {
  private apiUrl = '/api/approvals';
  
  constructor(private http: HttpClient) {}
  
  // API methods
  getPendingApprovals(...): Observable<ApprovalListResponse> { }
  
  // Utility methods
  getCurrentDateTime(): Date { }
  formatDate(date: Date): string { }
}
```

### Feature Module Pattern
Each feature lives in its own lazy-loaded module with:
- Feature component (list/container)
- Feature routing module
- Child components (create, edit, detail)
- Feature-specific services (if needed)

Example: customers.module.ts
```typescript
@NgModule({
  declarations: [
    CustomersComponent,
    CreateCustomerComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,        // For forms
    RouterModule.forChild([...]), // Feature routes
    SharedModule                 // Shared utilities
  ]
})
export class CustomersModule { }
```

## Routing Architecture

### Lazy Loading Strategy
All feature modules use lazy loading via `loadChildren`:
```typescript
{
  path: 'customers',
  loadChildren: () => import('./features/customers/customers.module').then(m => m.CustomersModule),
  data: { breadcrumb: 'Customers' }
}
```

### Route Data
- `breadcrumb`: Used by breadcrumb component for navigation trail
- `data` object can store role permissions, page titles, etc.

### Child Routes
Features define child routes in their own routing module:
```typescript
const routes: Routes = [
  { path: '', component: CustomersComponent },
  { path: 'create', component: CreateCustomerComponent }
];
```

## Component Architecture

### Component Decorator Pattern
```typescript
@Component({
  selector: 'app-[feature-name]',
  standalone: false,             // NgModule-based
  templateUrl: './[name].component.html',
  styleUrls: ['./[name].component.scss']
})
```

### Class Structure Standard Order
1. **Injected Services** — Constructor parameters
2. **@Input() / @Output()** — Component API
3. **FormGroup** — Reactive forms (if form component)
4. **State Properties** — Component state variables
5. **Constructor** — Dependency injection
6. **Lifecycle Hooks** — ngOnInit, ngOnDestroy, etc.
7. **Public Methods** — Event handlers, actions
8. **Private Helpers** — Utility methods

Example:
```typescript
export class CreateCustomerComponent {
  // 1. Injected services
  constructor(
    private fb: FormBuilder,
    private router: Router,
    private customerService: CustomerService
  ) {}

  // 2. Inputs/Outputs (if any)
  @Input() customerId?: string;
  @Output() saved = new EventEmitter<Customer>();

  // 3. FormGroup
  customerForm: FormGroup;

  // 4. State properties
  activeTab: 'postal' | 'swift' = 'postal';
  permissions: string[] = [];
  isLoading = false;

  // 5. Constructor (shown above)

  // 6. Lifecycle hooks
  ngOnInit(): void {
    this.initializeForm();
  }

  // 7. Public methods
  onSave(): void { /* ... */ }
  onCancel(): void { /* ... */ }

  // 8. Private helpers
  private initializeForm(): void { /* ... */ }
}
```

## Form Architecture (Reactive Forms)

### Module Import
```typescript
imports: [ReactiveFormsModule]
```

### FormBuilder Injection
```typescript
constructor(private fb: FormBuilder) {}
```

### Form Group Definition
```typescript
this.customerForm = this.fb.group({
  customerName: ['', Validators.required],
  email: ['', [Validators.required, Validators.email]],
  // Nested form groups
  address: this.fb.group({
    street: [''],
    city: ['', Validators.required]
  })
});
```

### Validation Helpers
```typescript
// Top-level field validation
isInvalid(controlName: string): boolean {
  const ctrl = this.customerForm.get(controlName);
  return !!(ctrl && ctrl.invalid && ctrl.touched);
}

// Nested form group validation
isGroupInvalid(groupName: string, controlName: string): boolean {
  const ctrl = this.customerForm.get(`${groupName}.${controlName}`);
  return !!(ctrl && ctrl.invalid && ctrl.touched);
}
```

### Form Submission
```typescript
onSave(): void {
  // Mark all fields as touched to show errors
  this.customerForm.markAllAsTouched();
  
  if (this.customerForm.valid) {
    const payload = this.customerForm.value;
    this.customerService.create(payload).subscribe({
      next: (response) => this.router.navigate(['/customers']),
      error: (err) => console.error('Save failed', err)
    });
  }
}
```

## Layout Architecture

### Three-Part Layout
1. **Sidebar** (left) — Navigation menu, collapsible
2. **Header** (top) — User info, breadcrumb, search
3. **Main Content** (center) — Router outlet for features

### LayoutComponent Structure
```html
<div class="layout">
  <app-sidebar></app-sidebar>
  <div class="layout-main">
    <app-header></app-header>
    <app-breadcrumb></app-breadcrumb>
    <main class="content">
      <router-outlet></router-outlet>
    </main>
  </div>
</div>
```

### Sidebar Navigation
- Menu items defined in component with route + icon
- Active state detection via Router.isActive
- Collapsed/expanded state (70px / 240px width)
- Gradient background on active item

## Shared Module Pattern

### Purpose
Export common directives, pipes, components for use across features.

### Structure
```typescript
@NgModule({
  declarations: [
    FormFieldDirective,
    SuccessComponent,
    // Add more shared directives/pipes/components
  ],
  imports: [CommonModule, RouterModule],
  exports: [
    FormFieldDirective,
    SuccessComponent,
    // Export for use in feature modules
  ]
})
export class SharedModule {}
```

### Shared Components

#### SuccessComponent
**Purpose:** Generic success confirmation page with navigation

**Location:** `src/app/shared/success/`

**Features:**
- Animated check icon with gradient background
- Customizable success message
- Configurable return URL
- Clean, centered layout with card design

**Usage:**
Navigate to success page with query parameters:
```typescript
// From any component
this.router.navigate(['/success'], {
  queryParams: {
    message: 'Customer created successfully!',
    returnUrl: '/customers'
  }
});
```

**Component Structure:**
```typescript
export class SuccessComponent implements OnInit {
  message: string = 'Operation completed successfully!';
  returnUrl: string = '/dashboard';

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['message']) {
        this.message = params['message'];
      }
      if (params['returnUrl']) {
        this.returnUrl = params['returnUrl'];
      }
    });
  }

  onReturn(): void {
    this.router.navigate([this.returnUrl]);
  }
}
```

**Styling Features:**
- Slide-up animation on page load
- Scale-in animation for success icon
- Gradient background for icon
- Responsive card layout
- Hover effects on return button
- CSS variables for consistent theming

**Route Configuration:**
```typescript
{
  path: 'success',
  component: SuccessComponent,
  data: { breadcrumb: 'Success' }
}
```

### Usage in Features
```typescript
imports: [
  CommonModule,
  ReactiveFormsModule,
  SharedModule  // Import to use appFormField directive and SuccessComponent
]
```

## Service Architecture (See 03-angular-service-standards.txt)
- Injectable pattern with `providedIn: 'root'`
- HttpClient for API integration
- Observable streams for async data
- Error handling with catchError

## Security Architecture

### Input Sanitization Directive
`appFormField` directive automatically:
- Strips XSS patterns (script tags, event handlers)
- Removes unicode abuse (emoji, special symbols)
- Prevents injection attacks (eval, alert, prompt)
- Applies on every keystroke
- Preserves cursor position

Applied to all inputs:
```html
<input appFormField formControlName="customerName" />
```

See: src/app/shared/directives/form-field.directive.ts

## Environment Configuration

### environment.ts (Development)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api',
  appName: 'Finastra Loan Service',
  version: '1.0.0'
};
```

### environment.prod.ts (Production)
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.production.com',
  appName: 'Finastra Loan Service',
  version: '1.0.0'
};
```

### Usage in Services
```typescript
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly baseUrl = `${environment.apiUrl}/customers`;
}
```

## Build & Deployment

### Development
```bash
npm start              # ng serve on localhost:4200
```

### Production Build
```bash
npm run build:prod     # ng build --configuration production
# Output: dist/loan.service.ui/
```

### Docker Build
Dockerfile includes:
1. Build stage (Node.js image)
2. Runtime stage (nginx image)
3. Copy dist files to nginx html folder
4. Custom nginx.conf for SPA routing

## Best Practices Summary

1. **Module Organization**
   - Lazy load all feature modules
   - Keep AppModule lean (only app-level imports)
   - Use SharedModule for common utilities

2. **Component Structure**
   - Follow standard class order
   - Keep components focused (single responsibility)
   - Use OnPush change detection when possible

3. **Forms**
   - Always use Reactive Forms
   - Apply appFormField to all inputs
   - Implement isInvalid helpers
   - Mark form touched before submission

4. **Routing**
   - Use lazy loading
   - Define breadcrumb data
   - Handle 404 with wildcard route

5. **Services**
   - providedIn: 'root' for singletons
   - Return observables, never subscribe in service
   - Handle errors with catchError

6. **Security**
   - Apply appFormField to all user inputs
   - Sanitize before API calls
   - Use environment variables for API URLs

7. **Styling**
   - Use CSS custom properties (design tokens)
   - Component-scoped SCSS files
   - Follow BEM or utility-first naming
