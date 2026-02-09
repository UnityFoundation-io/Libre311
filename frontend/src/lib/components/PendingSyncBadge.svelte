<script lang="ts">
	import { Badge } from 'stwui';

	let clazz: string = '';
	export { clazz as class };
</script>

<div class="pending-sync-badge-wrapper">
	<Badge class={clazz}>Pending Sync</Badge>
</div>

<style>
	.pending-sync-badge-wrapper {
		display: contents;
	}

	.pending-sync-badge-wrapper :global(.stwui-badge) {
		background-color: hsl(40, 90%, 50%);
	}
</style>
