// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
import type { ServiceDefinition } from '$lib/services/Libre311/Libre311';

declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		interface PageState {
			serviceDefinition?: ServiceDefinition;
		}
		// interface Platform {}
	}
}

export {};
