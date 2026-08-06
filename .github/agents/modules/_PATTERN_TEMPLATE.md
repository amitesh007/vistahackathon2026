# Pattern: [UI-NNN] [Pattern Name]

**ID**: UI-NNN
**Tags**: [comma-separated from taxonomy in PATTERN_REGISTRY.md]
**Angular Version**: [17 / 18 / 19 / 20 / 21]
**UI Library**: [angular-material / primeng / plain-scss]
**State Approach**: [rxjs / signals / ngrx / none]

---

## When to Apply

Describe the scenario where this pattern applies:
- What type of screen (form / table / dialog / wizard / etc.)
- What operations (create / read / update / delete)
- Any specific technical conditions

---

## File Structure

```
src/app/features/[feature-name]/
├── [screen-name].component.ts
├── [screen-name].component.html
├── [screen-name].component.scss
├── [screen-name].component.spec.ts
└── [screen-name].service.ts
```

---

## Non-Obvious Patterns

Document only patterns that are NOT obvious from reading the code.
Skip standard Angular patterns that are self-explanatory.

### [Pattern 1 Title]
Why: [Hidden constraint or subtle invariant]
```typescript
// Code snippet
```

### [Pattern 2 Title]
Why: [Hidden constraint or subtle invariant]
```typescript
// Code snippet
```

---

## Common Mistakes

Things that went wrong during implementation:
- [Mistake 1]: [What happens + correct approach]
- [Mistake 2]: [What happens + correct approach]

---

## Reference Files

- Component: `[path]`
- Service: `[path]`
- Spec: `[path]`

---

## Registered By

- Date: [YYYY-MM-DD]
- Story/Ticket: [reference or "none"]
