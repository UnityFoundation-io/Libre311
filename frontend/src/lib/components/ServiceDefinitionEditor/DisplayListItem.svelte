<script lang="ts">
	import { Dropdown } from 'stwui';
	import ToggleState from '../ToggleState.svelte';
	import EllipsisVertical from '../Svg/outline/EllipsisVertical.svelte';
	import ListItemContainer from './ListItemContainer.svelte';

	import ChevronRight from '../Svg/outline/ChevronRight.svelte';
    import {Button} from "$lib/components/ui/button";

	type DropDownProps = {
		text: string;
		action: () => void;
	}[];

	interface Props {
		href: string;
		dropDownItems?: DropDownProps;
		text?: import('svelte').Snippet;
	}

	let { href, dropDownItems = [], text }: Props = $props();
</script>

<ListItemContainer>
	{#snippet leading()}
		<div >
			{#if dropDownItems.length > 0}
				<ToggleState startingValue={false}  >
					{#snippet children({ show, toggle })}
								<Dropdown visible={show}>
							{#snippet trigger()}
										<Button variant="ghost"  on:click={toggle}>
									{#snippet icon()}
												<EllipsisVertical  />
											{/snippet}
								</Button>
									{/snippet}

							{#snippet items()}
										<Dropdown.Items  class="w-[100px]">
									{#each dropDownItems as item}
										<Button variant="ghost" class="w-full" on:click={item.action}>{item.text}</Button>
									{/each}
								</Dropdown.Items>
									{/snippet}
						</Dropdown>
												{/snippet}
						</ToggleState>
			{/if}
		</div>
	{/snippet}

	{#snippet main()}
		{@render text?.()}
	{/snippet}

	{#snippet trailing()}
		<div  class="mx-2 flex items-center justify-center">
			<Button variant="ghost" {href}>

						<ChevronRight  />

			</Button>
		</div>
	{/snippet}
</ListItemContainer>
