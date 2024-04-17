<script lang="ts">
	import { Button, Dropdown } from 'stwui';
	import ToggleState from '../ToggleState.svelte';
	import EllipsisVertical from '../Svg/outline/EllipsisVertical.svelte';
	import ListItemContainer from './ListItemContainer.svelte';

	import ChevronRight from '../Svg/outline/ChevronRight.svelte';

	type DropDownProps = {
		text: string;
		action: () => void;
	}[];

	export let href: string;
	export let dropDownItems: DropDownProps = [];
</script>

<ListItemContainer>
	<div slot="leading">
		{#if dropDownItems.length > 0}
			<ToggleState startingValue={false} let:show let:toggle>
				<Dropdown visible={show}>
					<Button type="ghost" slot="trigger" on:click={toggle}>
						<EllipsisVertical slot="icon" />
					</Button>

					<Dropdown.Items slot="items" class="w-[100px]">
						{#each dropDownItems as item}
							<Button type="ghost" class="w-full" on:click={item.action}>{item.text}</Button>
						{/each}
					</Dropdown.Items>
				</Dropdown>
			</ToggleState>
		{/if}
	</div>

	<slot name="text" slot="main" />

	<div slot="trailing" class="mx-2 flex items-center justify-center">
		<Button type="ghost" {href}>
			<ChevronRight slot="icon" />
		</Button>
	</div>
</ListItemContainer>
