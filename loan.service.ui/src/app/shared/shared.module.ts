import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormFieldDirective } from './directives/form-field.directive';
import { SuccessComponent } from './success/success.component';

@NgModule({
  declarations: [FormFieldDirective, SuccessComponent],
  imports: [CommonModule, RouterModule],
  exports: [FormFieldDirective, SuccessComponent]
})
export class SharedModule {}
