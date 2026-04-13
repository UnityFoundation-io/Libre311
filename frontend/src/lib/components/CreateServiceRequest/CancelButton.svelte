<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { Button } from 'stwui';

	const dispatch = createEventDispatcher<{ cancel: undefined }>();
</script>

<Button type="link" on:click={() => dispatch('cancel')}>Cancel</Button>
