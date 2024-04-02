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

	let input: HTMLInputElement;
	let reuploadInput: HTMLInputElement;
	let imageData: string | undefined;

	export let params: Readonly<Partial<CreateServiceRequestUIParams>>;

	const dispatch = createEventDispatcher();

	const linkResolver = useLibre311Context().linkResolver;

	const allowedExtensions = Libre311ServiceImpl.supportedImageTypes.map((s) => {
		return s.slice(s.indexOf('/') + 1);
	});

	async function onChange() {
		if (input.files && input.files.length < 2) dispatchFile(input.files[0]);
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
				const result: String = new String(reader.result);
				imageData = result.toString();
			};
		}
	});
</script>

<Breakpoint>
	<div slot="is-desktop" class="flex h-full w-full flex-col">
		<div class="flex h-screen flex-grow flex-col items-center justify-center">
			{#if imageData}
				<div class="relative mx-auto my-4 overflow-hidden">
					<img class="w-full" src={imageData} alt="preview" />
				</div>

				<div class="grid w-full grid-rows-3 gap-2">
					<input
						type="file"
						name="photo"
						id="camera-roll-btn-desktop"
						accept="image/*"
						hidden
						bind:this={input}
						on:change={onChange}
					/>
					<label for="camera-roll-btn-desktop">{messages['photo']['change_image']}</label>

					<Button
						type="ghost"
						on:click={() => {
							dispatch('stepChange');
						}}
					>
						{messages['photo']['use_current_image']}
					</Button>

					<Button
						type="ghost"
						on:click={() => {
							dispatch('stepChange', { file: undefined });
						}}
					>
						{messages['photo']['no_upload']}
					</Button>
				</div>
			{:else}
				<div class="items-center justify-center">
					<div class="mb-4">
						<FilePicker onDrop={desktopDropFiles} {allowedExtensions}>
							<FilePicker.Icon slot="icon" data={uploadIcon} />
							<FilePicker.Title slot="title">{messages['photo']['upload']}</FilePicker.Title>
							<FilePicker.Description slot="description"
								>Drag & Drop your file</FilePicker.Description
							>
						</FilePicker>
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
				<div class="relative mx-auto my-4 overflow-hidden rounded-lg">
					<img class="w-full" src={imageData} alt="preview"/>
				</div>

				<div class="grid w-full grid-rows-3 gap-2">
					<input
						class="w-full"
						type="file"
						name="photo"
						id="camera-roll-btn-reupload"
						accept="image/*"
						hidden
						bind:this={reuploadInput}
						on:change={reuploadImage}
					/>
					<label for="camera-roll-btn-reupload">{messages['photo']['change_image']}</label>

					<Button
						class="w-full"
						type="ghost"
						on:click={() => {
							dispatch('stepChange');
						}}
					>
						{messages['photo']['use_current_image']}
					</Button>

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
			{:else}
				<div class="grid grid-rows-2 gap-2">
					<input
						type="file"
						name="photo"
						id="camera-roll-btn"
						accept="image/*"
						hidden
						bind:this={input}
						on:change={onChange}
					/>
					<label class="w-full" for="camera-roll-btn">{messages['photo']['upload']}</label>

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
	img {
		object-fit: contain;
		max-height: 15rem;
	}
</style>
