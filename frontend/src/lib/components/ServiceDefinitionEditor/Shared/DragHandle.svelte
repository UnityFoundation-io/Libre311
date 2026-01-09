<script lang="ts">
	/**
	 * Whether the drag handle is currently being dragged
	 */
	export let isDragging = false;

	/**
	 * Additional CSS classes
	 */
	let className = '';
	export { className as class };

	/**
	 * Accessible label for the drag handle
	 */
	export let ariaLabel = 'Drag to reorder';
</script>

<div
	class="flex cursor-grab items-center justify-center text-gray-400 hover:text-gray-600 active:cursor-grabbing {isDragging
		? 'cursor-grabbing text-blue-600'
		: ''} {className}"
	role="button"
	tabindex="0"
	aria-label={ariaLabel}
	aria-grabbed={isDragging}
	on:mousedown
	on:touchstart
	on:keydown
	on:click
>
	<!-- Six-dot drag handle icon (::) -->
	<svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
		<circle cx="7" cy="5" r="1.5" />
		<circle cx="13" cy="5" r="1.5" />
		<circle cx="7" cy="10" r="1.5" />
		<circle cx="13" cy="10" r="1.5" />
		<circle cx="7" cy="15" r="1.5" />
		<circle cx="13" cy="15" r="1.5" />
	</svg>
</div>
