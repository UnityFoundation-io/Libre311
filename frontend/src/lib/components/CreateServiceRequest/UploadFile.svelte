<script lang="ts">
	import { Libre311ServiceImpl } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher, onMount } from 'svelte';
	import Breakpoint from '../Breakpoint.svelte';
	import messages from '$media/messages.json';
	import { FilePicker } from 'stwui';
	import { uploadIcon } from '$lib/components/Svg/outline/upload-icon.js';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import type { DropResult } from 'stwui/types';
	import { page } from '$app/stores';
	import type { CreateServiceRequestUIParams } from './shared';
	import Camera from '../Svg/outline/Camera.svelte';
	import Upload from '../Svg/outline/Upload.svelte';
    import {Button} from "$lib/components/ui/button";

	let input: HTMLInputElement = $state();
	let reuploadInput: HTMLInputElement = $state();
	let imageData: string | undefined = $state();

	interface Props {
		params: Readonly<Partial<CreateServiceRequestUIParams>>;
	}

	let { params }: Props = $props();

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
		if (dropFiles.rejected[0]) {
			console.log('Unsupported file type');
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
	<!-- @migration-task: migrate this slot by hand, `is-desktop` is an invalid identifier -->
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
						onchange={handleDesktopImageInput}
					/>
					<label for="camera-roll-btn-desktop">
						<div class="flex items-center justify-center">
							<Camera />
							<span class="ml-2">{messages['photo']['change_image']}</span>
						</div>
					</label>

					<Button
						variant="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>

					<Button
						variant="ghost"
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
							{#snippet icon()}
														<FilePicker.Icon  data={uploadIcon} />
													{/snippet}
							{#snippet title()}
														<FilePicker.Title >
									{messages['photo']['upload']}
								</FilePicker.Title>
													{/snippet}
							{#snippet description()}
														<FilePicker.Description >
									Drag & Drop your file
								</FilePicker.Description>
													{/snippet}
						</FilePicker>
					</div>

					<Button
						class="w-full"
						variant="ghost"
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
			variant="ghost"
			href={linkResolver.createIssuePagePrevious($page.url)}
		>
			{messages['photo']['back']}
		</Button>
	</div>

	<!-- @migration-task: migrate this slot by hand, `is-mobile-or-tablet` is an invalid identifier -->
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
						onchange={reuploadImage}
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
						onchange={handleMobileImageInput}
					/>
					<label for="re-upload-image-button">
						<div class="flex items-center justify-center">
							<Upload />
							<span class="ml-2">{messages['photo']['upload']}</span>
						</div>
					</label>

					<Button
						class="w-full"
						variant="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>

					<Button
						class="w-full"
						variant="ghost"
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
						onchange={handleMobileImageInput}
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
						onchange={handleMobileImageInput}
					/>
					<label class="w-full" for="upload-image-btn">
						<div class="flex items-center justify-center">
							<Upload />
							<span class="ml-2">{messages['photo']['upload']}</span>
						</div>
					</label>

					<Button
						class="w-full"
						variant="ghost"
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
			variant="ghost"
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
