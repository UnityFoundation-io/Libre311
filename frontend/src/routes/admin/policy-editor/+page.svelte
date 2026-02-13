<script lang="ts">
	import { onMount } from 'svelte';
	import { beforeNavigate, goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import MarkdownEditor from '$lib/components/MarkdownEditor.svelte';
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

	$: privacyDirty = privacyContent !== originalPrivacy;
	$: termsDirty = termsContent !== originalTerms;
	$: isDirty = privacyDirty || termsDirty;

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
				title: 'Change saved.'
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
						<span class="ml-1 inline-block h-2 w-2 rounded-full bg-orange-400" title="Unsaved changes"></span>
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
						<span class="ml-1 inline-block h-2 w-2 rounded-full bg-orange-400" title="Unsaved changes"></span>
					{/if}
				</button>
			</nav>
		</div>

		<!-- Editor panels -->
		<div class="mt-6">
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
			<SaveButton
				disabled={!isDirty}
				{isSaving}
				on:click={handleSave}
			/>
		</div>
	{/if}
</div>
