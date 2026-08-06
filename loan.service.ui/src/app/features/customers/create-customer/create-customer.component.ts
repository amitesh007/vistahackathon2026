import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-customer',
  standalone: false,
  templateUrl: './create-customer.component.html',
  styleUrls: ['./create-customer.component.scss']
})
export class CreateCustomerComponent {
  activeAddressTab: 'postal' | 'swift' = 'postal';
  permissions: string[] = [];

  customerForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router) {
    this.customerForm = this.fb.group({
      // General
      customerName: ['', Validators.required],
      abbreviatedName: ['', Validators.required],
      correspondenceLanguage: ['', Validators.required],
      baseCurrency: ['', Validators.required],
      // Postal Address
      postalAddress: this.fb.group({
        streetName: [''],
        town: [''],
        countrySubdivision: [''],
        postCode: [''],
        countryCode: ['', Validators.required],
        contractReference: ['']
      }),
      // SWIFT Address
      swiftAddress: this.fb.group({
        bic: [''],
        swiftName: [''],
        swiftCountry: ['']
      }),
      // Other Details
      contactName: [''],
      contactNumber: [''],
      sirenNbAndCountry: [''],
      fax: [''],
      telex: [''],
      bei: [''],
      email: ['', Validators.email],
      crmEmail: ['', Validators.email],
      webAddress: [''],
      legalIdType: [''],
      legalEntityIdentifier: ['']
    });
  }

  setTab(tab: 'postal' | 'swift'): void {
    this.activeAddressTab = tab;
  }

  isInvalid(controlName: string): boolean {
    const ctrl = this.customerForm.get(controlName);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  isGroupInvalid(groupName: string, controlName: string): boolean {
    const ctrl = this.customerForm.get(`${groupName}.${controlName}`);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  addPermission(): void {
    const label = `Permission ${this.permissions.length + 1}`;
    this.permissions.push(label);
  }

  removePermission(index: number): void {
    this.permissions.splice(index, 1);
  }

  onSave(): void {
    this.customerForm.markAllAsTouched();
    if (this.customerForm.valid) {
      console.log('Customer saved:', { ...this.customerForm.value, permissions: this.permissions });
      this.router.navigate(['/success'], {
        queryParams: {
          message: 'Customer created successfully!',
          returnUrl: '/customers'
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/customers']);
  }
}
