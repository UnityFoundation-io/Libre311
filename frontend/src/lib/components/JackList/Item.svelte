<script lang="ts">
	export let isActive = false;
</script>

<div class="jacklist-item">
	<slot name="content" />
</div>

<style>
	/* .jacklist-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		text-align: left;
		color: var(--color, hsl(220 20% 20%));
		margin: 0.375rem;
	} */

	.jacklist-item:hover {
		background-color: var(--accordion-hover, hsl(216, 100%, 90%));
	}

	.jacklist-item-caret {
	}

	.jacklist-item-content {
		color: var(--color, hsl(216, 84%, 73%));
		padding: 0;
	}
</style>
