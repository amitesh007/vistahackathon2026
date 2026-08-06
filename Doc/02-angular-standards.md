ANGULAR COMPONENT STANDARDS
============================

These conventions apply to all Angular components in the loan.service.ui project.

## Component Architecture
- NgModule-based (standalone: false on all components)
- Each feature has its own module in src/app/features/
- Shared directives and utilities live in src/app/shared/

## Component Decorator Pattern
@Component({
  selector: 'app-[screen-name]',
  standalone: false,
  templateUrl: './[name].component.html',
  styleUrls: ['./[name].component.scss']
})

## Class Structure Order
1. Injected services (private, via constructor)
2. Inputs and Outputs (@Input, @Output)
3. Reactive FormGroup (if form screen)
4. State properties
5. Constructor
6. Lifecycle hooks (ngOnInit, ngOnDestroy)
7. Public/protected event handlers
8. Private helpers

## Forms
- Always use ReactiveFormsModule (FormBuilder, FormGroup, Validators)
- Apply appFormField directive on all <input> and <textarea> elements
- The appFormField directive sanitizes input and manages invalid CSS class
- Required fields show error span: <span class="error-msg" *ngIf="isInvalid('field')">Required</span>
- isInvalid(controlName) checks control.invalid && control.touched
- isGroupInvalid(groupName, controlName) for nested FormGroup controls

## Routing
- Feature modules use lazy loading via loadChildren
- Routes defined in feature module with RouterModule.forChild()
- Child routes for sub-pages (e.g. /customers/create for create customer form)

## Styling Conventions
- Use CSS variables (--primary-color, --border-color, etc.)
- Inputs: class="form-input", height 38px, border: 1px solid var(--border-color)
- Focus state: border-color: var(--primary-color), box-shadow: 0 0 0 3px rgba(105,78,214,0.12)
- Primary buttons: gradient from --primary-color to --secondary-color
- Section titles: uppercase, 13px, font-weight 600, --text-secondary color
- Cards/panels: background var(--bg-primary), border-radius var(--radius-lg), box-shadow var(--shadow-sm)
- Form grids: CSS Grid, 4-column for general fields, 3-column for address fields

## Create Customer Form
Located at: /customers/create (route)
Component: src/app/features/customers/create-customer/create-customer.component.ts
Sections:
- General: Customer Name*, Abbreviated Name*, Correspondence Language*, Base Currency*
- Address (Postal/SWIFT tabs): Street Name, Town, Country Subdivision, Post Code, Country Code*, Contract Reference
- Other Details: Contact Name, Contact Number, SIREN nb and Country, Fax, Telex, BEI, E-mail, CRM E-mail, Web Address, Legal ID Type (select), Legal Entity Identifier
- Permission(s): dynamic list with Add/Remove
Footer: Cancel (navigates to /customers), Submit (validates and saves)

## Security — FormField Directive
Directive: appFormField (SharedModule)
Sanitizes input values against 14 XSS/injection regex patterns:
- Unicode emoji ranges
- HTML tag injections (<, %3C)
- JavaScript injections (eval(, alert, href, prompt)
- Event handler injections (onclick=, onload=)
- CSS/template injections (style=, {color:)
Applied to all inputs via appFormField attribute.

## Sidebar Navigation
Component: src/app/layout/sidebar/sidebar.component.ts
Menu items: Dashboard (/dashboard), Customers (/customers), Roles (/roles), Templates (/templates)
Active state: gradient background on active menu item
Collapsed state: icons only (70px wide), expanded: icons + labels (240px wide)
