# Libre311 Frontend

SvelteKit web application for Libre311 municipal service requests.

## Developing

Install dependencies and start a development server:

```bash
npm install
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Environment Variables

Copy `.env.example` to `.env` and configure the following variables:

| Variable                    | Description                    | Example                     |
| --------------------------- | ------------------------------ | --------------------------- |
| `VITE_BACKEND_URL`          | URL to the Libre311 API server | `http://localhost:8080/api` |
| `VITE_GOOGLE_RECAPTCHA_KEY` | Google reCAPTCHA site key      | `6Lc1rB4sAAAAA...`          |

### Geocoding Service

The frontend uses the backend API for reverse geocoding (converting coordinates to addresses). The backend proxies requests to [Nominatim](https://nominatim.openstreetmap.org/), which centralizes geocoding configuration and avoids CORS issues.

All geocoding requests go through:

```
{VITE_BACKEND_URL}/geocode/reverse?lat={lat}&lon={lon}
```

No additional frontend configuration is required for geocoding.

## Building

To create a production version of your app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.

## Testing

```bash
# Run all tests
npm run test

# Run unit tests only
npm run test:unit

# Run integration tests only
npm run test:integration
```

## Docker

For Docker deployments, use `.env.docker` to configure:

- Backend URL pointing to the API container
