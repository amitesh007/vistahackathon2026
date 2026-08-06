PROJECT OVERVIEW — Vista Hackathon 26
======================================

Project Name: Vista Hackathon 26
Repository: vistahackathon26

## Purpose
A full-stack loan service application consisting of:
1. Angular UI (loan.service.ui) — customer-facing frontend dashboard
2. Java Spring Boot API (LoanService) — REST backend
3. TypeScript Agent System (agents) — AI-powered orchestration layer

## Technology Stack

### Frontend (loan.service.ui)
- Angular (NgModule-based, non-standalone)
- SCSS for styling with CSS custom properties (design tokens)
- Reactive Forms (@angular/forms ReactiveFormsModule)
- Angular Router for navigation
- Primary color: #694ED6 (purple), secondary: #C137A2 (pink)

### Backend (LoanService)
- Java 17 with Spring Boot 3.2.5
- Gradle build system
- REST API endpoints (port 8081)
- H2 in-memory database
- CORS-enabled for Angular frontend

### Agent Orchestration (agents)
- TypeScript / Node.js
- Host Orchestrator pattern
- Agent Registry for dynamic agent discovery
- Pipeline Runner for sequential multi-agent workflows
- Microsoft GraphRAG for knowledge-base-augmented context

## Application Features
- Customer management (list, create)
- Dashboard with widgets (stats, charts, calendar, activity feed)
- Role management
- Template management
- Loan service operations
- Success confirmation page with navigation

## Design System
CSS variables defined in styles.scss:
- --primary-color: #694ED6
- --secondary-color: #C137A2
- --bg-primary: #FFFFFF
- --bg-secondary: #F7F8FC
- --text-primary: #1A1D1F
- --text-secondary: #6F767E
- --border-color: #EFEFEF
- --radius-sm/md/lg/xl for border radius
- --shadow-sm/md/lg for box shadows

## Project Structure
vistahackathon26/
├── loan.service.ui/     Angular frontend
├── LoanService/         Spring Boot backend
├── agents/              TypeScript AI agent system
├── knowledge-base/      GraphRAG knowledge base
├── helm/                Kubernetes Helm charts
└── .github/             Instructions, workflows, agent configs
