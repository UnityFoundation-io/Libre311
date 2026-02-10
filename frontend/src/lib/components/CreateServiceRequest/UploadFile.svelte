<script lang="ts">
	import { Libre311ServiceImpl } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher, onMount } from 'svelte';
	import Breakpoint from '../Breakpoint.svelte';
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
	import { FilePicker } from 'stwui';
	import { uploadIcon } from '$lib/components/Svg/outline/upload-icon.js';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import type { DropResult } from 'stwui/types';
	import { page } from '$app/stores';
	import type { CreateServiceRequestUIParams } from './shared';
	import Camera from '../Svg/outline/Camera.svelte';
	import Upload from '../Svg/outline/Upload.svelte';

	const maxFileSize = 8 * 1024 * 1024; // Files > 8MB will be resized
	const resizeMaxWidth = 4032; // Resized images will be cropped to fit within this width and height
	const resizeMaxHeight = 3024;
	const resizeQuality = 0.9;
	const resizeFormat = 'image/jpeg';

	let input: HTMLInputElement;
	let reuploadInput: HTMLInputElement;
	let imageData: string | undefined;
	let fileUploadError: string | undefined;
	let fileUploadErrorKey: number = 0;

	export let params: Readonly<Partial<CreateServiceRequestUIParams>>;

	const dispatch = createEventDispatcher();

	const linkResolver = useLibre311Context().linkResolver;

	const allowedExtensions = Libre311ServiceImpl.supportedImageTypes.map((s) => {
		return s.slice(s.indexOf('/') + 1);
	});

	function validateImage(file: File): boolean {
		return file.size <= maxFileSize;
	}

	function resizeImage(file: File): Promise<File> {
		return new Promise((resolve, reject) => {
			const img = new Image();
			const url = URL.createObjectURL(file);

			img.onload = () => {
				URL.revokeObjectURL(url);

				let { width, height } = img;

				const scale = Math.min(resizeMaxWidth / width, resizeMaxHeight / height, 1);

				width *= scale;
				height *= scale;

				const canvas = document.createElement('canvas');
				canvas.width = width;
				canvas.height = height;

				const ctx = canvas.getContext('2d');
				if (!ctx) return reject();

				ctx.drawImage(img, 0, 0, width, height);

				canvas.toBlob(
					(blob) => {
						if (!blob) return reject();
						resolve(new File([blob], file.name, { type: resizeFormat }));
					},
					resizeFormat,
					resizeQuality
				);
			};

			img.onerror = reject;
			img.src = url;
		});
	}

	async function attemptResize(fileToResize: File | undefined) {
		if (!fileToResize) return;

		try {
			return await resizeImage(fileToResize);
		} catch (error) {
			fileUploadError = messages['photo']['invalid_file_type'];
			fileUploadErrorKey = Date.now();
		}
	}

	async function dispatchValidImage(imageFile: File) {
		if (!validateImage(imageFile)) {
			const resized = await attemptResize(imageFile);
			if (!resized) return;

			dispatchFile(resized);
		} else {
			fileUploadError = undefined;
			dispatchFile(imageFile);
		}
	}

	async function handleDesktopImageInput() {
		if (input.files && input.files.length < 2) {
			fileUploadError = undefined;
			await dispatchValidImage(input.files[0]);
		}
	}

	async function handleMobileImageInput(e: Event) {
		const input = e.target as HTMLInputElement;

		if (input.files) {
			fileUploadError = undefined;
			await dispatchValidImage(input.files[0]);
		}
	}

	async function reuploadImage() {
		if (reuploadInput.files && reuploadInput.files.length < 2) {
			fileUploadError = undefined;
			await dispatchValidImage(reuploadInput.files[0]);
		}
	}

	async function desktopDropFiles(dropFiles: DropResult) {
		if (dropFiles.accepted && dropFiles.accepted.length > 0) {
			fileUploadError = undefined;
			dispatchFile(dropFiles.accepted[0]);
		} else if (dropFiles.rejected && dropFiles.rejected.length > 0) {
			fileUploadError = undefined;
			const resized = await attemptResize(dropFiles.rejected[0]);
			if (!resized) return;
			dispatchFile(resized);
		}
	}

	function dispatchFile(file: File) {
		dispatch('stepChange', { file });
	}

	onMount(() => {
		if (params.file) {
			let reader = new FileReader();
			reader.readAsDataURL(params.file);

			reader.onloadend = function () {
				const result: string = String(reader.result);
				imageData = result.toString();
			};
		}
	});
</script>

<Breakpoint>
	<div slot="is-desktop" class="flex h-full w-full flex-col">
		<div class="flex h-screen flex-grow flex-col items-center justify-center">
			{#if imageData}
				<div class="image-container relative mx-auto my-4">
					<img class="rounded-lg" src={imageData} alt="preview" />
				</div>

				<div class="grid w-full grid-rows-3 gap-2">
					<input
						type="file"
						name="photo"
						id="camera-roll-btn-desktop"
						accept="image/*"
						hidden
						bind:this={input}
						on:change={handleDesktopImageInput}
					/>
					<label for="camera-roll-btn-desktop">
						<div class="flex items-center justify-center">
							<Camera />
							<span class="ml-2">{messages['photo']['change_image']}</span>
						</div>
					</label>

					<label for="re-upload-image-button">
						<div class="flex items-center justify-center">
							<Upload />
							<span class="ml-2">{messages['photo']['upload']}</span>
						</div>
					</label>

					<Button
						type="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>

					<Button
						type="ghost"
						on:click={() => {
							dispatch('stepChange');
						}}
					>
						{messages['photo']['use_current_image']}
					</Button>
				</div>
			{:else}
				<div class="items-center justify-center">
					<div class="mb-4">
						<FilePicker {maxFileSize} onDrop={desktopDropFiles} {allowedExtensions}>
							<FilePicker.Icon slot="icon" data={uploadIcon} />
							<FilePicker.Title slot="title">
								{messages['photo']['upload']}
							</FilePicker.Title>
							<FilePicker.Description slot="description">
								Drag & Drop your file
							</FilePicker.Description>
						</FilePicker>
						{#if fileUploadError}
							{#key fileUploadErrorKey}
								<p class="stwui-input-error mt-2 text-center text-sm text-danger" role="alert">
									{fileUploadError}
								</p>
							{/key}
						{/if}
					</div>

					<Button
						class="w-full"
						type="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>
				</div>
			{/if}
		</div>

		<Button
			class="mb-4 flex w-14 justify-start"
			type="ghost"
			href={linkResolver.createIssuePagePrevious($page.url)}
		>
			{messages['photo']['back']}
		</Button>
	</div>

	<div slot="is-mobile-or-tablet" class="flex h-full w-full flex-col">
		<div class="flex h-screen flex-grow flex-col items-center justify-center">
			{#if imageData}
				<div class="image-container relative mx-auto my-4">
					<img class="rounded-lg" src={imageData} alt="preview" />
				</div>

				<div class="grid w-full grid-rows-4 gap-2">
					<input
						class="w-full"
						type="file"
						name="photo"
						id="re-take-photo-button"
						accept="image/*"
						capture="environment"
						hidden
						bind:this={reuploadInput}
						on:change={reuploadImage}
					/>
					<label for="re-take-photo-button">
						<div class="flex items-center justify-center">
							<Camera />
							<span class="ml-2">{messages['photo']['change_image']}</span>
						</div>
					</label>

					<input
						class="w-full"
						type="file"
						name="photo"
						id="re-upload-image-button"
						accept="image/*"
						hidden
						on:change={handleMobileImageInput}
					/>
					<label for="re-upload-image-button">
						<div class="flex items-center justify-center">
							<Upload />
							<span class="ml-2">{messages['photo']['upload']}</span>
						</div>
					</label>

					{#if fileUploadError}
						{#key fileUploadErrorKey}
							<p class="stwui-input-error mt-2 text-center text-sm text-danger" role="alert">
								{fileUploadError}
							</p>
						{/key}
					{/if}

					<Button
						class="w-full"
						type="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>

					<Button
						class="w-full"
						type="ghost"
						on:click={() => {
							dispatch('stepChange');
						}}
					>
						{messages['photo']['use_current_image']}
					</Button>
				</div>
			{:else}
				<div class="grid grid-rows-2 gap-2">
					<input
						type="file"
						name="photo"
						id="camera-roll-btn"
						accept="image/*"
						capture="environment"
						hidden
						bind:this={input}
						on:change={handleMobileImageInput}
					/>
					<label class="w-full" for="camera-roll-btn">
						<div class="flex items-center justify-center">
							<Camera />
							<span class="ml-2">{messages['photo']['take_photo']}</span>
						</div>
					</label>

					<input
						type="file"
						name="photo"
						id="upload-image-btn"
						accept="image/*"
						hidden
						bind:this={input}
						on:change={handleMobileImageInput}
					/>
					<label class="w-full" for="upload-image-btn">
						<div class="flex items-center justify-center">
							<Upload />
							<span class="ml-2">{messages['photo']['upload']}</span>
						</div>
					</label>

					{#if fileUploadError}
						{#key fileUploadErrorKey}
							<p class="stwui-input-error mt-2 text-center text-sm text-danger" role="alert">
								{fileUploadError}
							</p>
						{/key}
					{/if}

					<Button
						class="w-full"
						type="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>
				</div>
			{/if}
		</div>

		<Button
			class="mb-4 flex w-14 justify-start"
			type="ghost"
			href={linkResolver.createIssuePagePrevious($page.url)}
		>
			{messages['photo']['back']}
		</Button>
	</div>
</Breakpoint>

<style>
	label {
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		padding: 0.5rem;
		font-family: sans-serif;
		text-align: center;
		border-radius: 0.3rem;
		cursor: pointer;
	}
	.image-container {
		display: flex;
		justify-content: center;
	}
	img {
		max-height: 20rem;
	}
</style>
