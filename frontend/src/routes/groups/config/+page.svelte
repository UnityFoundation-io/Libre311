<script lang="ts">
	// Service Definition Configuration Page
	// Split-pane editor with tree navigation and card-based editing
</script>

<svelte:head>
	<title>Service Definition Configuration</title>
</svelte:head>

<div class="flex h-screen flex-col">
	<!-- Header -->
	<header class="border-b border-gray-200 bg-white px-6 py-4">
		<div class="flex items-center gap-4">
			<a
				href="/groups"
				class="flex items-center gap-2 text-gray-600 hover:text-gray-900"
				aria-label="Back to Admin"
			>
				<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M10 19l-7-7m0 0l7-7m-7 7h18"
					/>
				</svg>
			</a>
			<h1 class="text-xl font-semibold text-gray-900">Service Definition Configuration</h1>
		</div>
	</header>

	<!-- Main Content - Split Pane Layout will go here -->
	<main class="flex-1 overflow-hidden bg-gray-50">
		<div class="flex h-full items-center justify-center text-gray-500">
			<p>Split-pane editor loading...</p>
		</div>
	</main>
</div>
