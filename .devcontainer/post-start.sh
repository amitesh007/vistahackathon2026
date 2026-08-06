#!/usr/bin/env bash
# .devcontainer/post-start.sh
# ──────────────────────────────────────────────────────────────────────────────
# Runs once the Codespace is ready (postStartCommand in devcontainer.json).
# Prints the public forwarded-port URLs for the browser and Postman.
# ──────────────────────────────────────────────────────────────────────────────

set -euo pipefail

# GitHub Codespaces injects $CODESPACE_NAME automatically.
# Fall back to "localhost" when running the devcontainer locally.
CS_NAME="${CODESPACE_NAME:-localhost}"

if [ "$CS_NAME" = "localhost" ]; then
  BACKEND_URL="http://localhost:8080"
  FRONTEND_URL="http://localhost:4200"
else
  BACKEND_URL="https://${CS_NAME}-8080.app.github.dev"
  FRONTEND_URL="https://${CS_NAME}-4200.app.github.dev"
fi

cat <<EOF

╔══════════════════════════════════════════════════════════════════╗
║              LoanService + UI  —  Service URLs                   ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  loan.service.ui  (browser)                                      ║
║    ${FRONTEND_URL}
║                                                                  ║
║  LoanService REST API  (Postman / cURL / browser)                ║
║    ${BACKEND_URL}
║                                                                  ║
║  H2 Console  (in-memory database)                                ║
║    ${BACKEND_URL}/h2-console
║                                                                  ║
╠══════════════════════════════════════════════════════════════════╣
║  Quick Postman / cURL examples                                   ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  # Health check                                                  ║
║  curl ${BACKEND_URL}/actuator/health
║                                                                  ║
║  # List all loans                                                ║
║  curl ${BACKEND_URL}/api/loan
║                                                                  ║
║  # Create a loan (replace body with real payload)                ║
║  curl -X POST ${BACKEND_URL}/api/loan \\
║       -H 'Content-Type: application/json' \\
║       -d '{}'                                                    ║
║                                                                  ║
║  # API via UI nginx proxy                                        ║
║  curl ${FRONTEND_URL}/api/loan
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝

EOF
