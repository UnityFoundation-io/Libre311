import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		// adapter-auto only supports some environments, see https://kit.svelte.dev/docs/adapter-auto for a list.
		// If your environment is not supported or you settled on a specific environment, switch out the adapter.
		// See https://kit.svelte.dev/docs/adapters for more information about adapters.
		adapter: adapter({
			fallback: "index.html"
		}),
		csp: {
			mode: "auto",
			directives: {
				'script-src': ['self', 'https://www.google.com/', 'https://maps.googleapis.com/',
					'https://www.gstatic.com/recaptcha/', 'https://maps.gstatic.com/', 'https://fonts.googleapis.com/']
			},
		},
    alias: {
      $media: "src/media/*"
    }
	}
};

export default config;
