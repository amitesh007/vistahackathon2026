# Vista Hackathon 26 — Knowledge Base

## Purpose
This knowledge base contains comprehensive documentation for generating code for the Vista Hackathon 26 loan service application. It covers both the Angular frontend (loan.service.ui) and Java Spring Boot backend (LoanService).

## Target Audience
This documentation is designed for:
- AI agents generating code
- Developers working on the project
- Code reviewers ensuring consistency
- New team members onboarding

## How to Use This Knowledge Base

### For AI Agents
1. **Start with 00-index.txt** — Quick reference and navigation guide
2. **Read relevant documents** based on the task at hand
3. **Follow templates in 10-code-generation-guide.txt** when generating code
4. **Refer to specific sections** for detailed patterns

### For Developers
1. **Read 00-index.txt** for an overview of all documents
2. **Consult topic-specific documents** when implementing features
3. **Use as reference** for consistent code patterns
4. **Update documentation** when new patterns are introduced

## Document Structure

### Core Documents
- **00-index.txt** — Master index with quick reference
- **01-project-overview.txt** — Project structure and tech stack
- **10-code-generation-guide.txt** — Complete templates for code generation

### Frontend Documentation (Angular)
- **02-angular-standards.txt** — Component standards and conventions
- **03-angular-service-standards.txt** — Service layer patterns
- **05-angular-ui-architecture.txt** — Comprehensive UI architecture

### Backend Documentation (Java)
- **06-java-backend-architecture.txt** — Spring Boot architecture
- **09-database-patterns.txt** — JPA, Hibernate, and H2 patterns

### Integration & Security
- **07-api-integration-patterns.txt** — Frontend-backend integration
- **08-security-patterns.txt** — Security measures across projects

### Specialized Topics
- **04-agent-architecture.txt** — AI agent orchestration (if applicable)

## Key Patterns to Follow

### Angular
- NgModule-based architecture (standalone: false)
- Lazy-loaded feature modules
- Reactive Forms with validation
- appFormField directive on all inputs
- Observable streams (never subscribe in services)

### Java
- className-based dynamic service resolution
- BaseIntegrationService pattern
- JpaRepository + custom repository pattern
- Input validation in service layer
- Parameterized queries for security

### Integration
- Environment-based configuration
- ISO 8601 date formats
- Consistent field naming (camelCase)
- CORS configuration
- Error handling on both sides

## Quick Navigation

### I want to...
- **Create an Angular component** → Read 05, 10
- **Create a Java entity** → Read 06, 09, 10
- **Create an Angular service** → Read 03, 05, 10
- **Create a Java service** → Read 06, 10
- **Integrate frontend with backend** → Read 07
- **Implement a form** → Read 02, 05
- **Secure the application** → Read 08
- **Work with the database** → Read 09

## Code Quality Standards
All generated code must:
- Follow existing project patterns
- Include all required files (no partial implementations)
- Have proper error handling
- Include TypeScript types / Java generics
- Apply security patterns (validation, sanitization)
- Be fully tested (include test files)
- Have clear documentation

## Version Control
- **Angular:** 21.0.0
- **Java:** 17
- **Spring Boot:** 3.2.5
- **Last Updated:** 2026-08-06

## Contributing
When updating this knowledge base:
1. Maintain consistency with existing patterns
2. Update the index (00-index.txt) when adding new documents
3. Include practical examples
4. Test all code templates before documenting
5. Keep documentation concise and actionable

## Support
For questions or clarifications:
- Refer to the relevant knowledge base document
- Check the index (00-index.txt) for quick reference
- Review existing code in the project for examples

---

**Remember:** This knowledge base is the source of truth for code generation patterns in this project. Always refer to it before creating new code to ensure consistency and quality.
