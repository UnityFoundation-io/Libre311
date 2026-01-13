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

	let input: HTMLInputElement;
	let reuploadInput: HTMLInputElement;
	let imageData: string | undefined;
	let filePickerError: string | undefined;

	export let params: Readonly<Partial<CreateServiceRequestUIParams>>;

	const dispatch = createEventDispatcher();

	const linkResolver = useLibre311Context().linkResolver;

	const allowedExtensions = Libre311ServiceImpl.supportedImageTypes.map((s) => {
		return s.slice(s.indexOf('/') + 1);
	});

	async function handleDesktopImageInput() {
		if (input.files && input.files.length < 2) dispatchFile(input.files[0]);
	}

	async function handleMobileImageInput(e: Event) {
		const input = e.target as HTMLInputElement;

		if (input.files) {
			dispatchFile(input.files?.[0]);
		}
	}

	async function reuploadImage() {
		if (reuploadInput.files && reuploadInput.files.length < 2) dispatchFile(reuploadInput.files[0]);
	}

	function desktopDropFiles(dropFiles: DropResult) {
		filePickerError = undefined;
		if (dropFiles.rejected[0]) {
			filePickerError = messages['photo']['invalid_file_type'];
		}
		for (const file of dropFiles.accepted) {
			dispatchFile(file);
			return; // Only upload single file
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
						<FilePicker onDrop={desktopDropFiles} {allowedExtensions}>
							<FilePicker.Icon slot="icon" data={uploadIcon} />
							<FilePicker.Title slot="title">
								{messages['photo']['upload']}
							</FilePicker.Title>
							<FilePicker.Description slot="description">
								Drag & Drop your file
							</FilePicker.Description>
						</FilePicker>
						{#if filePickerError}
							<p class="stwui-input-error mt-2 text-center text-sm text-danger" role="alert">
								{filePickerError}
							</p>
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
