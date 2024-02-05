import { writable } from 'svelte/store';

export const serviceRequestImageUpload = writable();

export const stageImage = (file: string) => {
	serviceRequestImageUpload.update(image => image = file);
}
