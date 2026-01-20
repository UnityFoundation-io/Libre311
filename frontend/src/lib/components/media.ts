// media.ts
import { readable } from 'svelte/store';

export function mediaQuery(query: string) {
	return readable(false, (set) => {
		if (typeof window === 'undefined') return;

		const mql = window.matchMedia(query);
		set(mql.matches);

		const handler = (e: MediaQueryListEvent) => set(e.matches);
		mql.addEventListener('change', handler);

		return () => mql.removeEventListener('change', handler);
	});
}
