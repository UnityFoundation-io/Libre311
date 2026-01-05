<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { Breadcrumbs, Button, Card } from 'stwui';
	import SaveToast from './SaveToast.svelte';

	/**
	 * Group ID for breadcrumb navigation
	 */
	export let groupId: number | undefined = undefined;

	/**
	 * Group name for breadcrumb display
	 */
	export let groupName = '';

	/**
	 * Service name for breadcrumb display
	 */
	export let serviceName = '';

	/**
	 * Whether the editor is in a loading state
	 */
	export let isLoading = false;

	/**
	 * Error message to display
	 */
	export let error: string | null = null;

	/**
	 * Whether to show the save toast
	 */
	export let showSaveToast = true;

	const dispatch = createEventDispatcher<{
		back: void;
	}>();

	interface Crumb {
		label: string;
		href: string;
	}

	$: breadcrumbs = buildBreadcrumbs(groupId, groupName, serviceName);

	function buildBreadcrumbs(
		groupId: number | undefined,
		groupName: string,
		serviceName: string
	): Crumb[] {
		const crumbs: Crumb[] = [{ label: 'Groups', href: '/groups' }];

		if (groupId !== undefined && groupName) {
			crumbs.push({
				label: groupName,
				href: `/groups/${groupId}`
			});
		}

		if (serviceName) {
			crumbs.push({
				label: serviceName,
				href: '#' // Current page
			});
		}

		return crumbs;
	}

	function handleBack() {
		dispatch('back');
	}

	function handleKeydown(event: KeyboardEvent) {
		// Allow Escape key to go back
		if (event.key === 'Escape') {
			handleBack();
		}
	}
</script>

<svelte:window on:keydown={handleKeydown} />

<div class="flex min-h-screen flex-col bg-gray-50">
	<!-- Navigation Header -->
	<Card bordered={true} class="m-4 mb-0">
		<Card.Header slot="header" class="flex items-center justify-between py-3">
			<div class="flex items-center gap-4">
				<!-- Back Button -->
				<Button
					type="ghost"
					class="flex items-center gap-1 text-gray-600 hover:text-gray-900"
					on:click={handleBack}
					aria-label="Go back to services list"
				>
					<svg
						class="h-5 w-5"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
						aria-hidden="true"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 19l-7-7 7-7"
						/>
					</svg>
					<span class="hidden sm:inline">Back</span>
				</Button>

				<!-- Breadcrumbs -->
				<Breadcrumbs>
					{#each breadcrumbs as crumb, index}
						<Breadcrumbs.Crumb href={crumb.href}>
							<Breadcrumbs.Crumb.Label slot="label">
								<span
									class={index === breadcrumbs.length - 1
										? 'font-semibold text-gray-900'
										: 'text-gray-600'}
								>
									{crumb.label}
								</span>
							</Breadcrumbs.Crumb.Label>
						</Breadcrumbs.Crumb>
					{/each}
				</Breadcrumbs>
			</div>
		</Card.Header>
	</Card>

	<!-- Main Content Area -->
	<main class="flex-1 overflow-y-auto px-4 pb-24 pt-4" aria-label="Service editor">
		{#if isLoading}
			<div class="flex items-center justify-center py-12">
				<div class="flex flex-col items-center gap-4">
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
					<span class="text-gray-600">Loading service definition...</span>
				</div>
			</div>
		{:else if error}
			<div
				class="mx-auto max-w-2xl rounded-lg border border-red-200 bg-red-50 p-6"
				role="alert"
				aria-live="assertive"
			>
				<div class="flex items-start gap-3">
					<svg
						class="h-6 w-6 flex-shrink-0 text-red-600"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
						/>
					</svg>
					<div>
						<h3 class="font-semibold text-red-800">Error loading service</h3>
						<p class="mt-1 text-red-700">{error}</p>
						<Button type="ghost" class="mt-3 text-red-700 hover:text-red-900" on:click={handleBack}>
							Return to services list
						</Button>
					</div>
				</div>
			</div>
		{:else}
			<!-- Scrollable content area with max width for readability -->
			<div class="mx-auto max-w-3xl">
				<slot />
			</div>
		{/if}
	</main>

	<!-- Save Toast -->
	{#if showSaveToast}
		<SaveToast />
	{/if}
</div>
