<script lang="ts">
	import { Button, Dropdown } from 'stwui';
	import { getJackListOptions } from './context';
	import { ellipsisSVG } from '../Svg/outline/EllipsisVertical.svelte';

	export let isActive = false;

	const componentId = crypto.randomUUID();

	const { activeComponentId } = getJackListOptions();

	$: isActive = $activeComponentId == componentId;
	// setActiveComponentId();
	$: console.log('Active Component ID:', $activeComponentId);
	$: console.log(isActive);
</script>

<div class="jacklist-item">
	<slot name="content" />
	<slot name="icon" />

	<Dropdown bind:visible={isActive}>
		<Button
			slot="trigger"
			type="ghost"
			shape="circle"
			on:click={() => {
				isActive = !isActive;
			}}
		>
			<Button.Icon data={ellipsisSVG} />
		</Button>

		<Dropdown.Items slot="items" class="w-[100px]">
			<Button type="ghost" class="w-full">Edit</Button>
		</Dropdown.Items>
	</Dropdown>
</div>

<style>
	.jacklist-item {
		display: flex;
		justify-content: space-between;
		text-align: left;
		color: var(--color, hsl(220 20% 20%));
		margin: 0.375rem;
	}

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
