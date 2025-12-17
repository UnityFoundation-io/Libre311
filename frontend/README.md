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

| Variable                    | Description                                                     | Example                     |
| --------------------------- | --------------------------------------------------------------- | --------------------------- |
| `VITE_BACKEND_URL`          | URL to the Libre311 API server                                  | `http://localhost:8080/api` |
| `VITE_GOOGLE_RECAPTCHA_KEY` | Google reCAPTCHA site key                                       | `6Lc1rB4sAAAAA...`          |
| `VITE_NOMINATIM_MODE`       | Set to `mock` to use fixture data instead of live Nominatim API | `mock` or empty             |

### Nominatim Service

The frontend uses [Nominatim](https://nominatim.openstreetmap.org/) for reverse geocoding (converting coordinates to addresses).

- **Development**: Requests are proxied through Vite to bypass CORS restrictions
- **Docker**: Set `VITE_NOMINATIM_MODE=mock` in `.env.docker` to use fixture data
- **Production**: Requests go directly to Nominatim (or your configured endpoint)

To use mock data during development or testing:

```bash
VITE_NOMINATIM_MODE=mock
```

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
- Optional Mock Nominatim service to avoid external dependencies
