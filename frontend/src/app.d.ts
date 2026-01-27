// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}

	// Google reCAPTCHA Enterprise global
	const grecaptcha: {
		enterprise: {
			execute(siteKey: string, options: { action: string }): Promise<string>;
			ready(callback: () => void): void;
		};
	};
}

export {};
