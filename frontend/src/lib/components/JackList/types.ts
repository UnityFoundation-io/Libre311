import type { Writable } from 'svelte/store';

export type JackListOptions = {};
export type ActiveComponentId = string | null;
export type ActiveComponentIdContext = Writable<ActiveComponentId>;
