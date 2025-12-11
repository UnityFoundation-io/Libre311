<script lang="ts">

	// import ToggleState from '../ToggleState.svelte';
	import EllipsisVertical from '../Svg/outline/EllipsisVertical.svelte';
	import ListItemContainer from './ListItemContainer.svelte';
	import { createEventDispatcher } from 'svelte';
	import XMark from '../Svg/outline/XMark.svelte';
	import CheckMark from '../Svg/outline/CheckMark.svelte';
	import { createInput, inputValidatorFactory } from '$lib/utils/validation';
	import { z } from 'zod';

    import * as DropdownMenu from "$lib/components/ui/dropdown-menu";
    import {Button} from "$lib/components/ui/button";
    import {Input} from "$lib/components/ui/input";

	const minLengthStringValidator = inputValidatorFactory(z.string().min(1));
	const dispatch = createEventDispatcher<{
		toggleEdit: void;
		confirm: { newText: string };
		close: void;
	}>();

	interface Props {
		startingText: string;
	}

	let { startingText }: Props = $props();

	let editListItemInput = $derived(createInput(startingText));

	function handleSubmit() {
		editListItemInput = minLengthStringValidator(editListItemInput);
		if (editListItemInput.type != 'valid') return;

		dispatch('confirm', { newText: editListItemInput.value });
	}
</script>

<ListItemContainer>
	{#snippet leading()}
		<div >
						<DropdownMenu.Root>

						    <DropdownMenu.Trigger>
                                {#snippet child({props})}
								<Button variant="ghost"  {...props}>
										<EllipsisVertical  />
							    </Button>
                                {/snippet}
                            </DropdownMenu.Trigger>

                            <DropdownMenu.Content>
                                <DropdownMenu.Item onSelect={() => dispatch('toggleEdit')}>Edit</DropdownMenu.Item>
                            </DropdownMenu.Content>
					</DropdownMenu.Root>
		</div>
	{/snippet}

	{#snippet main()}
		<Input
			
			class="w-full"
			type="text"
			name="new-service-name"
			bind:value={editListItemInput.value}
		></Input>
	{/snippet}

	{#snippet trailing()}
		<div  class="mx-2 flex items-center justify-center">
			<Button
				aria-label="Close"
				variant="ghost"
				on:click={() => {
					dispatch('close');
				}}
			>
						<XMark  />
			</Button>

			<Button aria-label="Submit" variant="ghost" on:click={handleSubmit}>
						<CheckMark  />
			</Button>
		</div>
	{/snippet}
</ListItemContainer>
