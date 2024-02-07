// https://vitejs.dev/guide/env-and-mode#modes
// https://vitejs.dev/guide/env-and-mode#env-variables
const modeList = ['development', 'test', 'production'] as const;
function isMode(maybeMode: unknown): maybeMode is Mode {
	const asString = String(maybeMode);
	return modeList.includes(asString as Mode);
}

export function getModeFromEnv(env: ImportMetaEnv): Mode {
	const mode = env.MODE;
	if (!isMode(mode)) throw new Error('Unsupported Mode');
	return mode;
}

export type Mode = (typeof modeList)[number];
