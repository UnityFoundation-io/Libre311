<script lang="ts">
	import { Button, Dropdown, Input } from 'stwui';
	import ToggleState from '../ToggleState.svelte';
	import EllipsisVertical from '../Svg/outline/EllipsisVertical.svelte';
	import ListItemContainer from './ListItemContainer.svelte';
	import { createEventDispatcher } from 'svelte';
	import XMark from '../Svg/outline/XMark.svelte';
	import CheckMark from '../Svg/outline/CheckMark.svelte';
	import { createInput, inputValidatorFactory } from '$lib/utils/validation';
	import { z } from 'zod';

	const minLengthStringValidator = inputValidatorFactory(z.string().min(1));
	const dispatch = createEventDispatcher<{
		toggleEdit: void;
		confirm: { newText: string };
		close: void;
	}>();

	export let startingText: string;

	$: editListItemInput = createInput(startingText);

	function handleSubmit() {
		editListItemInput = minLengthStringValidator(editListItemInput);
		if (editListItemInput.type != 'valid') return;

		dispatch('confirm', { newText: editListItemInput.value });
	}
</script>

<ListItemContainer>
	<div slot="leading">
		<ToggleState startingValue={false} let:show let:toggle>
			<Dropdown visible={show}>
				<Button type="ghost" slot="trigger" on:click={toggle}>
					<EllipsisVertical slot="icon" />
				</Button>

				<Dropdown.Items slot="items" class="w-[100px]">
					<Button type="ghost" class="w-full" on:click={() => dispatch('toggleEdit')}>Edit</Button>
				</Dropdown.Items>
			</Dropdown>
		</ToggleState>
	</div>

	<Input
		slot="main"
		class="w-full"
		type="text"
		name="new-service-name"
		error={editListItemInput.error}
		bind:value={editListItemInput.value}
	></Input>

	<div slot="trailing" class="mx-2 flex items-center justify-center">
		<Button
			aria-label="Close"
			type="ghost"
			on:click={() => {
				dispatch('close');
			}}
		>
			<XMark slot="icon" />
		</Button>

		<Button aria-label="Submit" type="ghost" on:click={handleSubmit}>
			<CheckMark slot="icon" />
		</Button>
	</div>
</ListItemContainer>
