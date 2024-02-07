import { writable, type Writable } from 'svelte/store';

export const serviceRequestFile: Writable<File> = writable();

export const stageFile = (file: File) => {
	serviceRequestFile.update(image => image = file);
}
