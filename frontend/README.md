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

- Concurrently
- Cross-env
- Flatpickr
- Fontfaceobserver
- Google Location Library (locations)
- Google Maps API (@googlemaps/js-api-loader)
- Google OAuth 2
- Google Recaptcha
- Google Safe Search
- Google Visualization Library (heat map)
- Leaflet
- Leaflet Control Geocoder
- Leaflet Geosearch
- Leaflet Heat
- Svelte-multiselect

### Setting Secrets

In the root directory, create a file called `setenv.sh`. Copy the contents from `setenv.sh.example` into `setenv.sh`, replacing values in carets with the corresponding values.

### Running Locally

With `setenv.sh` populated with relevant keys, run `source setenv.sh` in root, followed by `./gradlew app:run`.

## Customization

### Skinning

- Color palette: `/lib/colors.json`
- Styling: `/lib/global.css`
- Icons: `/icons`
- Library folder: `/lib`
- Fonts: `/static/font`

Use `.env` to store Google Keys and the backend reference. The keys can be found in the root frontend folder `.env.example`.

The following assets are stored by default in `frontend/src/media`:

- Background: `backgroundImage.webp`
- Footer links: `footerLinks.json`
- Footer logo: `footerLogoSmall.webp`
- Organization logo: `logo.webp`
- Externalized Messages: `messages.json`

To change any of these elements to fit around specific organization material, these files can be directly altered (in the case of .json files), or replaced with files of the same name (in the case of .webp files). 

Alternatively, a new folder can be created with these files named accordingly within. To change which folder is read for assets, the following line from `frontend/svelte.config.js` can be changed to the appropriate file path:

```js
alias: {
  $media: "src/media/*"
}
```
where `src/media/*` can be replaced with the path to the newly created folder.

#### A Note on Images

In order to insure faster load times, it's recommended to use WebP image files instead of JPEGs or PNGs due to their smaller file size. For example, the default `backgroundImage.webp` was originally a 2.8mb JPEG, but it is currently a 348kb WebP (approximately 88% smaller).

If the desired images are not currently in WebP format, there are multiple tools online that allow the conversion from many different file types to .webp.

### Background Image

To change the background image, remove the current `backgroundImage.webp` from `frontend/src/media`, and put a new image in it's place (remember to rename the new file to `backgroundImage.webp`). 

If a custom media directory is used, simply put the new file in that directory (make sure the file is named `backgroundImage.webp`)

***Notice**: The image should be in a portrait orientation. The default image has a resolution of 816x1456 px.*

### Logo

To change the organization logo, remove the current `logo.webp` from `frontend/src/media`, and put a new image in it's place (remember to rename the new file to `logo`).

If a custom media directory is used, simply put the new file in that directory (make sure the file is named `logo.webp`)

***Notice**: The default image has a resolution of 800x450 px.*

### Footer

The footer of the app currently displays links to various pages from LocalMotion, as well as their social media links.

To edit the content of the footer, go to `frontend/src/media/footerLinks.json.` The structure of the file is set up as:

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

If a custom media directory is used, simply copy `footerLinks.json` from `frontend/src/media` into that directory, and edit the content accordingly. Alternatively, create a new `footerLinks.json` in the new directory, using the structure above to fill the footer with custom links.

To use a custom footer icon, remove the current `footerLogoSmall.webp` from `frontend/src/media`, and put a new image in it's place (remember to rename the new file to `footerLogoSmall.webp`.). 

If a custom media directory is used, simply put the new file in that directory (make sure the file is named `footerLogoSmall.webp`)

***Notice**: The default image has a resolution of 72x72 px.*

For social media links, see `frontend/src/lib/Footer.svelte`. The buttons for social media (Facebook, Instagram, and Twitter) are in place, but without links by default. 

```html
<div class="socials">
  <a class="socialButton" href="">
    <img src="{facebookSVG}" alt="facebook" />
  </a>
  <a
    class="socialButton" href="">
    <img src="{instagramSVG}" alt="instagram" />
  </a>
  <a class="socialButton" href="">
    <img src="{twitterSVG}" alt="twitter" />
  </a>
</div>
```

To connect each button to a respective social media page, place the appropriate page link in the `href` for each respective social media button.

### Color Schema

Hex values for the app's color scheme are saved in `/frontend/src/lib/colors.json`. To adjust the color scheme to closer align with brand standards, edit these values accordingly. 

### Maintaining State

SvelteKit uses the concept of 'stores' to maintain the frontend state, even as some DOM elements are destroyed or the user navigates to another screen. These stores, located in the `/stores` folder, can be initialized with a value or return null.

### Select Map Provider

The maps can be provided by OpenStreetMaps/Leaflet or Google Maps, depending on preference. Within `/frontend/src/lib/mapProvider.json`, the value of `provider` can be set to either `googleMaps` or `osm` for OpenStreetMaps.

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
