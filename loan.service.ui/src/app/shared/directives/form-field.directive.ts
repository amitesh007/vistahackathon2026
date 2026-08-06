import { Directive, HostBinding, HostListener, Self, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Sanitization patterns to strip XSS, injection, and unicode abuse vectors.
 * Each regex is applied sequentially to the raw input value.
 */
const SANITIZE_PATTERNS: RegExp[] = [
  /[\u2580-\u27BF]/gu,                        // Misc unicode block/drawing symbols
  /[\u{1F300}-\u{1FAFF}]/gu,                  // Emoji ranges
  /top\[/gi,                                   // window.top access attempt
  /\.vibrate\(/gi,                             // Vibration API abuse
  /\\u\p{N}+/gu,                              // Raw unicode escape sequences (\u0041)
  /alert|href/gi,                              // XSS vectors
  /eval\(+/gi,                                 // Code injection
  /(?:<|%3C)(?=[%!?/a-zA-Z])/gi,             // HTML tag open (<, %3C)
  /style=/gi,                                  // CSS injection
  /prompt/gi,                                  // Social engineering dialog
  /on\w+=/gi,                                  // Inline event handlers (onclick=, onload=)
  /(?:>|%3E){2,}|(?:<|%3C){2,}/gi,           // Repeated HTML bracket sequences
  /[\w\W]="/gi,                                // Attribute injection (x=")
  /[{|=][a-zA-Z]+:/gi,                         // Template/CSS injection ({color:, =background:)
];

/**
 * Applies to <input> and <textarea> elements inside reactive forms.
 * - Sanitizes the value on every keystroke, removing patterns that indicate
 *   XSS, injection, or unicode abuse attacks.
 * - Automatically applies the `invalid` CSS class when the bound control
 *   is invalid and touched (no need for [class.invalid] in the template).
 * - Marks the control as touched on blur so required-field errors surface.
 *
 * Usage:
 *   <input appFormField formControlName="fieldName" ... />
 */
@Directive({
  selector: 'input[appFormField], textarea[appFormField]',
  standalone: false
})
export class FormFieldDirective {

  constructor(@Self() @Optional() private ngControl: NgControl) {}

  /** Automatically adds/removes `invalid` class based on control state */
  @HostBinding('class.invalid')
  get isInvalid(): boolean {
    const ctrl = this.ngControl?.control;
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  /** Sanitize value on every keystroke */
  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = event.target as HTMLInputElement | HTMLTextAreaElement;
    const original = input.value;
    const sanitized = this.sanitize(original);
    if (sanitized !== original) {
      // Preserve cursor position by computing offset from end
      const fromEnd = original.length - (input.selectionEnd ?? original.length);
      input.value = sanitized;
      const newPos = Math.max(0, sanitized.length - fromEnd);
      input.setSelectionRange(newPos, newPos);
      // Patch the reactive form control silently so change detection picks it up
      this.ngControl?.control?.setValue(sanitized, { emitEvent: true });
    }
  }

  /** Mark control as touched on blur so required errors surface */
  @HostListener('blur')
  onBlur(): void {
    this.ngControl?.control?.markAsTouched();
  }

  private sanitize(value: string): string {
    if (!value) { return value; }
    let result = value;
    for (const pattern of SANITIZE_PATTERNS) {
      // Re-create regex each pass to reset lastIndex for global patterns
      result = result.replace(new RegExp(pattern.source, pattern.flags), '');
    }
    return result;
  }
}
