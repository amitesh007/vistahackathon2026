# Loan Service UI

Production-ready Angular application for managing loan services.

## Quick Start

### Development

```bash
# Install dependencies
npm install

# Start development server
npm start
```

Visit `http://localhost:4200`

### Production Build

```bash
# Build for production
npm run build:prod

# Build and run with Docker
docker build -t loan-service-ui .
docker run -p 4200:80 loan-service-ui

# Or use Docker Compose
docker-compose up -d
```

## Features

✅ **Dashboard** - Interactive dashboard with real-time metrics
✅ **Breadcrumb Navigation** - Context-aware navigation
✅ **Responsive Design** - Mobile-first approach
✅ **Production Ready** - Optimized builds with Docker support
✅ **TypeScript** - Strong typing for better development experience
✅ **Modular Architecture** - Feature-based structure

## Project Structure

```
loan.service.ui/
├── src/
│   ├── app/
│   │   ├── core/           # Core services and utilities
│   │   ├── shared/         # Shared components
│   │   ├── features/       # Feature modules
│   │   │   ├── dashboard/  # Dashboard feature
│   │   │   ├── customers/  # Customer management
│   │   │   ├── roles/      # Role management
│   │   │   └── templates/  # Template management
│   │   └── layout/         # Layout components
│   ├── assets/             # Static assets
│   ├── environments/       # Environment configs
│   └── styles.scss         # Global styles
├── Dockerfile              # Production Docker image
├── nginx.conf              # Nginx configuration
└── angular.json            # Angular CLI config
```

## Technology Stack

- **Angular 21** - Modern web framework
- **TypeScript 5.9** - Type-safe development
- **SCSS** - Enhanced CSS with variables
- **RxJS** - Reactive programming
- **Docker** - Containerization
- **Nginx** - Production web server

## Development Guidelines

### Path Aliases

Use TypeScript path aliases for cleaner imports:

```typescript
import { SomeService } from '@core/services/some.service';
import { SharedComponent } from '@shared/components/shared.component';
import { DashboardComponent } from '@features/dashboard/dashboard.component';
```

### Component Structure

Each feature module follows this structure:

```
feature/
├── feature.module.ts
├── feature.component.ts
├── feature.component.html
├── feature.component.scss
├── feature.component.spec.ts
└── widgets/
    └── widget-name/
```

### Coding Standards

- Use Angular style guide conventions
- Follow TypeScript strict mode
- Write unit tests for components
- Use semantic versioning
- Document public APIs

## Deployment

### Docker Deployment

```bash
# Build image
docker build -t loan-service-ui:1.0.0 .

# Run container
docker run -d -p 4200:80 --name loan-ui loan-service-ui:1.0.0
```

### Kubernetes Deployment

```bash
# Build and tag
docker build -t your-registry/loan-service-ui:1.0.0 .
docker push your-registry/loan-service-ui:1.0.0

# Deploy to Kubernetes
kubectl apply -f k8s/
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `apiUrl` | Backend API URL | `http://localhost:3000/api` |
| `production` | Production mode | `false` |
| `appName` | Application name | `Finastra Loan Service` |

## Scripts

- `npm start` - Start development server
- `npm run build` - Build for development
- `npm run build:prod` - Build for production
- `npm test` - Run unit tests
- `npm run lint` - Lint code

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

Proprietary - All rights reserved

## Support

For issues and questions, contact the development team.
