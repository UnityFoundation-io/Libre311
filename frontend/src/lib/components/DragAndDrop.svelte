<script lang="ts" generics="T">
	import { createEventDispatcher } from 'svelte';
	// we have to disable the eslint warning because it doesn't recognize the svelte generics syntax above even though it is valid
	// eslint-disable-next-line no-undef
	type ItemsArray = T[];

	const dispatch = createEventDispatcher<{ itemsChanged: ItemsArray }>();

	export let items: Readonly<ItemsArray>;

	let draggingIndex: number | undefined = undefined;
	let internalItems: ItemsArray;

	// this doesn't work
	//$: internalItems = [...items];

	// but this does, not sure why
	$: updateInternalItems(items);
	function updateInternalItems(items: Readonly<ItemsArray>) {
		internalItems = [...items];
	}

	function handleDragStart(index: number) {
		draggingIndex = index;
	}

	function handleDragOver(e: DragEvent, index: number) {
		e.preventDefault();
		if (draggingIndex === undefined || draggingIndex === index) return;
		// we must get a reference to the item being dragged before mutating the array
		const draggingItem = internalItems[draggingIndex];

		internalItems.splice(draggingIndex, 1);
		internalItems.splice(index, 0, draggingItem);
		draggingIndex = index;
		internalItems = internalItems;
	}

	function handleDragEnd() {
		draggingIndex = undefined;
		dispatch('itemsChanged', internalItems);
	}
</script>

{#each internalItems as item, index}
	<div
		role="listitem"
		class="drag-item {draggingIndex === index && 'dragging'}"
		draggable="true"
		on:dragstart={() => handleDragStart(index)}
		on:dragover={(event) => handleDragOver(event, index)}
		on:dragend={handleDragEnd}
	>
		<slot name="item" {item} />
	</div>
{/each}

{JSON.stringify(internalItems, null, 2)}

<style>
	.drag-item.dragging {
		opacity: 0.2;
	}
</style>
