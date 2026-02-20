<script lang="ts">
	import { onMount, tick } from 'svelte';
	import { beforeNavigate, goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import MarkdownEditor from '$lib/components/MarkdownEditor.svelte';
	import MarkdownRenderer from '$lib/components/MarkdownRenderer.svelte';
	import SaveButton from '$lib/components/ServiceDefinitionEditor/Shared/SaveButton.svelte';
	import type { LibrePermissions } from '$lib/services/Libre311/Libre311';

	const { service, alertError, alert, user } = useLibre311Context();

	const requiredPermissions: LibrePermissions[] = [
		'LIBRE311_ADMIN_EDIT-SYSTEM',
		'LIBRE311_ADMIN_EDIT-TENANT',
		'LIBRE311_ADMIN_EDIT-SUBTENANT'
	];

	$: if ($user !== undefined) {
		const hasPermission = $user?.permissions.some((p) => requiredPermissions.includes(p));
		if (!hasPermission) {
			goto('/');
		}
	}

	type Tab = 'privacy' | 'terms';

	let activeTab: Tab = 'privacy';
	let isLoading = true;
	let loadError: string | null = null;
	let isSaving = false;

	// Current editor values
	let privacyContent = '';
	let termsContent = '';

	// Original values (last saved state)
	let originalPrivacy = '';
	let originalTerms = '';

	// Editor refs for imperative reset
	let privacyEditorRef: MarkdownEditor;
	let termsEditorRef: MarkdownEditor;

	let closePreviewButton: HTMLElement;

	$: privacyDirty = privacyContent !== originalPrivacy;
	$: termsDirty = termsContent !== originalTerms;
	$: isDirty = privacyDirty || termsDirty;
	$: if (showPreview) {
		tick().then(() => closePreviewButton?.focus());
	}

	onMount(async () => {
		await loadContent();
	});

	async function loadContent() {
		isLoading = true;
		loadError = null;
		try {
			const backendUrl = import.meta.env.VITE_BACKEND_URL || '/api';
			const response = await fetch(`${backendUrl}/config`, {
				headers: {
					Referer: typeof window !== 'undefined' ? window.location.href : ''
				}
			});

			if (!response.ok) {
				throw new Error(`Failed to load: ${response.status} ${response.statusText}`);
			}

			const data = await response.json();
			privacyContent = data.privacy_policy_content ?? '';
			termsContent = data.terms_of_use_content ?? '';
			originalPrivacy = privacyContent;
			originalTerms = termsContent;
		} catch (err) {
			console.error('Failed to load policy content:', err);
			loadError = 'Failed to load current policy content. You can still edit and save.';
		} finally {
			isLoading = false;
		}
	}

	function handlePrivacyChange(event: CustomEvent<string>) {
		privacyContent = event.detail;
	}

	function handleTermsChange(event: CustomEvent<string>) {
		termsContent = event.detail;
	}

	async function handleSave() {
		isSaving = true;
		try {
			await service.updatePolicyContent({
				privacy_policy_content: privacyContent || null,
				terms_of_use_content: termsContent || null
			});

			originalPrivacy = privacyContent;
			originalTerms = termsContent;

			alert({
				type: 'success',
				title: 'Change saved.',
				description: 'Your changes have been saved successfully.'
			});
		} catch (err) {
			alertError(err);
		} finally {
			isSaving = false;
		}
	}

	function handleCancel() {
		privacyContent = originalPrivacy;
		termsContent = originalTerms;

		if (privacyEditorRef) {
			privacyEditorRef.reset(originalPrivacy);
		}
		if (termsEditorRef) {
			termsEditorRef.reset(originalTerms);
		}
	}

	function setTab(tab: Tab) {
		activeTab = tab;
	}

	// Preview modal
	$: showPreview = false;
	$: previewContent = activeTab === 'privacy' ? privacyContent : termsContent;
	$: previewTitle = activeTab === 'privacy' ? 'Privacy Policy' : 'Terms of Use';

	// File upload
	let fileInput: HTMLInputElement;

	function handleUploadClick() {
		fileInput.click();
	}

	function handleFileSelected(event: Event) {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;

		const reader = new FileReader();
		reader.onload = (e) => {
			const content = e.target?.result as string;
			if (activeTab === 'privacy') {
				privacyContent = content;
				privacyEditorRef?.reset(content);
			} else {
				termsContent = content;
				termsEditorRef?.reset(content);
			}
		};
		reader.readAsText(file);

		// Reset so the same file can be re-selected
		input.value = '';
	}

	// Warn on browser navigation (refresh, close tab)
	function handleBeforeUnload(e: BeforeUnloadEvent) {
		if (isDirty) {
			e.preventDefault();
		}
	}

	// Warn on SvelteKit client-side navigation
	beforeNavigate(({ cancel }) => {
		if (isDirty && !confirm('You have unsaved changes. Are you sure you want to leave?')) {
			cancel();
		}
	});
</script>

<svelte:window on:beforeunload={handleBeforeUnload} />

<svelte:head>
	<title>Privacy Policy/T&C Editor</title>
</svelte:head>

<div class="mx-auto max-w-5xl px-4 py-6">
	<!-- Header -->
	<div class="mb-6">
		<h1 class="text-2xl font-semibold text-gray-900">Privacy Policy/T&C Editor</h1>
		<p class="mt-1 text-sm text-gray-500">
			Edit the privacy policy and terms of use for your jurisdiction.
		</p>
	</div>

	{#if loadError}
		<div class="mb-4 rounded-md border border-yellow-300 bg-yellow-50 p-4 text-sm text-yellow-800">
			{loadError}
		</div>
	{/if}

	{#if isLoading}
		<div class="flex items-center justify-center py-16">
			<svg
				class="h-8 w-8 animate-spin text-blue-600"
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
			>
				<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
				></circle>
				<path
					class="opacity-75"
					fill="currentColor"
					d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
				></path>
			</svg>
			<span class="ml-3 text-gray-600">Loading...</span>
		</div>
	{:else}
		<!-- Tabs -->
		<div class="border-b border-gray-200">
			<nav class="-mb-px flex gap-6" aria-label="Policy tabs">
				<button
					type="button"
					class="whitespace-nowrap border-b-2 px-1 pb-3 text-sm font-medium transition-colors {activeTab ===
					'privacy'
						? 'border-blue-500 text-blue-600'
						: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
					on:click={() => setTab('privacy')}
					aria-selected={activeTab === 'privacy'}
					role="tab"
				>
					Privacy Policy
					{#if privacyDirty}
						<span
							class="ml-1 inline-block h-2 w-2 rounded-full bg-orange-400"
							title="Unsaved changes"
						></span>
					{/if}
				</button>
				<button
					type="button"
					class="whitespace-nowrap border-b-2 px-1 pb-3 text-sm font-medium transition-colors {activeTab ===
					'terms'
						? 'border-blue-500 text-blue-600'
						: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
					on:click={() => setTab('terms')}
					aria-selected={activeTab === 'terms'}
					role="tab"
				>
					Terms of Use
					{#if termsDirty}
						<span
							class="ml-1 inline-block h-2 w-2 rounded-full bg-orange-400"
							title="Unsaved changes"
						></span>
					{/if}
				</button>
			</nav>
		</div>

		<!-- Toolbar -->
		<div class="mt-4 flex gap-2">
			<input
				bind:this={fileInput}
				type="file"
				accept=".md,.markdown,.txt"
				class="hidden"
				on:change={handleFileSelected}
			/>
			<button
				type="button"
				class="inline-flex items-center gap-1.5 rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
				on:click={handleUploadClick}
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
					stroke-width="1.5"
					stroke="currentColor"
					class="h-4 w-4"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5m-13.5-9L12 3m0 0 4.5 4.5M12 3v13.5"
					/>
				</svg>
				Upload File
			</button>
			<button
				type="button"
				class="inline-flex items-center gap-1.5 rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
				on:click={() => (showPreview = true)}
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
					stroke-width="1.5"
					stroke="currentColor"
					class="h-4 w-4"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z"
					/>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
					/>
				</svg>
				Preview
			</button>
		</div>

		<!-- Editor panels -->
		<div class="mt-4">
			<div class:hidden={activeTab !== 'privacy'}>
				<MarkdownEditor
					bind:this={privacyEditorRef}
					value={privacyContent}
					placeholder="Enter privacy policy content in Markdown..."
					on:change={handlePrivacyChange}
				/>
			</div>
			<div class:hidden={activeTab !== 'terms'}>
				<MarkdownEditor
					bind:this={termsEditorRef}
					value={termsContent}
					placeholder="Enter terms of use content in Markdown..."
					on:change={handleTermsChange}
				/>
			</div>
		</div>

		<!-- Action buttons -->
		<div class="mt-6 flex items-center justify-end gap-3">
			<button
				type="button"
				class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
				on:click={handleCancel}
				disabled={!isDirty || isSaving}
			>
				Cancel
			</button>
			<span title={!isDirty ? 'Make changes to enable saving' : ''}>
				<SaveButton disabled={!isDirty} {isSaving} on:click={handleSave} />
			</span>
		</div>
	{/if}
</div>

<!-- Preview modal -->
{#if showPreview}
	<!-- svelte-ignore a11y-click-events-have-key-events a11y-no-static-element-interactions -->
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
		on:click|self={() => (showPreview = false)}
	>
		<div
			class="relative mx-4 flex max-h-[85vh] w-full max-w-3xl flex-col rounded-lg bg-white shadow-xl"
		>
			<!-- Modal header -->
			<div class="flex items-center justify-between border-b px-6 py-4">
				<h2 class="text-lg font-semibold text-gray-900">Preview: {previewTitle}</h2>
				<button
					bind:this={closePreviewButton}
					type="button"
					class="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
					on:click={() => (showPreview = false)}
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="1.5"
						stroke="currentColor"
						class="h-5 w-5"
					>
						<path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
					</svg>
				</button>
			</div>
			<!-- Modal body -->
			<div class="overflow-y-auto px-6 py-4">
				{#if previewContent}
					<MarkdownRenderer markdown={previewContent} />
				{:else}
					<p class="text-sm italic text-gray-400">No content to preview.</p>
				{/if}
			</div>
		</div>
	</div>
{/if}
