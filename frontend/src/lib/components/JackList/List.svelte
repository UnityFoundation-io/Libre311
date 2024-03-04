<script lang="ts">
	import type { SelectOption } from 'stwui/types';
	import Item from './Item.svelte';
	import { Button } from 'stwui';
	import { ellipsisSVG } from '../Svg/outline/EllipsisVertical.svelte';
	import { getJackListOptions } from './context';

	export let items: SelectOption[];
</script>

<div class="jacklist">
	{#each items as item, i}
		<Item isActive={false}>
			<div slot="content">{item.label}</div>
			<!-- <div slot="icon">
				<Button slot="trigger" type="ghost" shape="circle" on:click={handleExpandMenu}>
					<Button.Icon data={ellipsisSVG} />
				</Button>
			</div> -->
		</Item>
	{/each}
</div>

<style>
	.jacklist {
		display: flex;
		flex-direction: column;
		width: var(--list-width, 100%);
		padding: var(--list-padding, 1rem);
		color: var(--list-color, --primary);
		background-color: var(--list-background-color, --surface);
		border-radius: var(--list-border-radius, 4px);
		border: 1px solid rgb(0 0 0 / 0.1);
		box-shadow:
			0 4px 6px -1px rgb(0 0 0 / 0.1),
			0 2px 4px -2px rgb(0 0 0 / 0.1);
		/* box-shadow: var(--list-box-shadow, 0px 1px 15px hsl(220 10% 10%)); */
	}
</style>
