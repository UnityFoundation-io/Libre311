# Local Motion We Move

## Front-end

### Overview

The user interface is developed using SvelteKit. We only serve the home route and construct the two distinct application flows atop this single route. This design pattern was selected to provide the user with a sensation of navigating around the background image through animations and transitions, delivering a unique browsing experience.

The Single Page Application (SPA) preloads Google Maps API and Recaptcha upon mounting to make them available for both flows. These APIs are loaded asynchronously using defer, which adds to the homepage's weight but allows for high responsiveness as the user navigates the flows.

Our primary font is Gotham, with Roboto and Helvetica serving as fallbacks. All assets are built statically and served from the backend upon deployment. Images are in webp format for optimal performance. The application supports both online and offline use, provided it was used at least once online with assets cached by the service worker. It will automatically adjust to network changes and store any issues reported offline, submitting them when online.

Upon load, the application fades in and out, darkening to increase the contrast ratio between the background and content. Adjust these values within the top-level components defining these Svelte animations/transitions.

### Progressive Web App (PWA) Design

We handle online/offline mode using a combination of local storage and service workers. `/src/service-worker.js` implements this feature.

### External Dependencies

- Google Recaptcha
- Google Maps API (@googlemaps/js-api-loader)
- Google Location Library (locations)
- Google Visualization Library (heat map)
- Google Safe Search
- Google OAuth 2
- Flatpickr
- Svelte-multiselect
- Cross-env
- Concurrently
- Fontfaceobserver

### Setting Secrets

In the root directory, create a file called `setenv.sh`. This file will contain any keys and secrets pertaining to online services using the following structure:

```bash
#!/bin/bash

export GCP_PROJECT_ID=<PROJECT_ID>
export MICRONAUT_ENVIRONMENTS=dev,dev-data
export MICRONAUT_SERVER_PORT=8080

export STORAGE_BUCKET_ID=<STORAGE_BUCKET_ID>
export GOOGLE_CLIENT_ID=<GOOGLE_CLIENT_ID>
export GOOGLE_CLIENT_SECRET=<GOOGLE_CLIENT_SECRET>
export RECAPTCHA_SECRET=<RECAPTCHA_SECRET>
export SAFESEARCH_KEY=<SAFESEARCH_KEY>
```

### Running Locally

With `setenv.sh` populated with relevant keys, run `source setenv.sh` in root, followed by `./gradlew app:run`.

## Customization

### Skinning

- Externalized Messages: `/lib/messages.json`
- Color Palette: `/lib/messages.json`
- Styling: `/lib/global.css`
- Icons: `/icons`
- Library folder: `/lib`
- Fonts: `/static/font`

Use `.env` to store Google Keys and the backend reference. The keys can be found in the root frontend folder `.env.example`.

#### A Note on Images

In order to insure faster load times, it's recommended to use WebP image files instead of JPEGs or PNGs due to their smaller file size. For example, the default `backgroundImage.webp` was originally a 2.8mb JPEG, but it is currently a 348kb WebP (approximately 88% smaller).

If the desired images are not currently in WebP format, there are multiple tools online that allow the conversion from many different file types to .webp.

### Background Image

The image used in the background of the app is located in `frontend/src/lib/backgroundImage.webp`. To change the background image, remove the current `backgroundImage.webp`, and put a new image in it's place (remember to rename the new file to `backgroundImage`).

***Notice**: The image should be in a portrait orientation. The default image has a resolution of 1024x1792 px.*

### Logo

The image used for the company logo is located in `frontend/src/lib/logo.webp`. To change the logo, remove the current `logo.webp`, and put a new image in it's place (remember to rename the new file to `logo`).

***Notice**: The default image has a resolution of 800x450 px.*

### Footer

The footer of the app currently displays links to various pages from LocalMotion, as well as their social media links.

To edit the content of the footer, go to `frontend/src/lib/footerLinks.json.` The structure of the file is set up as:

```json
[
  {
    "header": "<HEADER TEXT>",
    "headerUrl": "<HEADER URL>",
    "links": [
      {
        "text": "<LINK TEXT>",
        "url": "<LINK URL>"
      },
      ...
    ]
  },
  ...
]
```

To create a new header with it's own set of links, copy this structure in `footerLinks.json`, and replace the text wrapped in carets.

The image on top of the footer is stored in `/frontend/src/lib/footerLogoSmall.webp`. To use a custom image, replace the default `footerLogoSmall.webp` with a new image, and rename that file's name to `footerLogoSmall`. 

***Notice**: The default image has a resolution of 72x72 px.*

### Color Schema

Hex values for the app's color scheme are saved in `/frontend/src/lib/colors.json`. To adjust the color scheme to closer align with brand standards, edit these values accordingly. 

### Maintaining State

SvelteKit uses the concept of 'stores' to maintain the frontend state, even as some DOM elements are destroyed or the user navigates to another screen. These stores, located in the `/stores` folder, can be initialized with a value or return null.

## Front-end Testing

WeMove uses Playwright for front-end testing.

### Configuration

Playwright configuration: `frontend/playwright.config.js`

GitHub CI configuration: `.github/workflows/playwright.yml`

### Writing Tests

Tests are stored in `frontend/tests`. For files required to run tests (like images for upload), see `frontend/tests/fixtures`.

Test template:

```javascript
test("test name", async ({ page }) => {
  await page.goto("http://localhost:3000/");
  await page
    .getByRole("element", { name: "element name or identifier" })
    .click();
});
```

### Automated Test Generation

To generate tests automatically, navigate to the frontend directory and run the following command:

`npx playwright codegen`

This will allow the generation of tests based on direct user input.

For in-depth guides for writing tests and the full functionality of Playwright, visit [Playwright Documentation](https://playwright.dev/docs/intro).

### Running Tests

To run all tests from the command line, in the frontend directory:

`npx playwright test`

To run tests from a specific file:

`npx playwright test <filename>`

To run a specific test:

`npx playwright test -g "test title"`

### Viewing Test Outputs

After a test run, the results are detailed in `playwright-report/index.html`
