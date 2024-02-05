import { writable, type Writable } from 'svelte/store';

export const serviceRequestImageUpload: Writable<string> = writable('');

export const stageImage = (file: string) => {
	serviceRequestImageUpload.update(image => image = file);
}
