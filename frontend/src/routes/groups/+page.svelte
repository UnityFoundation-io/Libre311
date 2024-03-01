<script lang="ts">
	import { goto } from '$app/navigation';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Input, List } from 'stwui';
	import { slide } from 'svelte/transition';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [{ label: 'Groups', href: '/groups' }];

	const groups = [
		{
			id: 1,
			title: 'Parks Department'
		},
		{
			id: 2,
			title: 'Traffic'
		}
	];

	let isDropdownVisible = false;
	let newGroupName: FormInputValue<string> = createInput();

	async function handleAddNewGroup() {
		newGroupName = stringValidator(newGroupName);

		if (newGroupName.type != 'valid') {
			return;
		}
		newGroupName.value = '';
		isDropdownVisible = false;
	}
</script>

<Card bordered={true} class="m-4">
	<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
		<Breadcrumbs>
			{#each crumbs as crumb}
				<Breadcrumbs.Crumb href={crumb.href}>
					<Breadcrumbs.Crumb.Label slot="label"><h3>{crumb.label}</h3></Breadcrumbs.Crumb.Label>
				</Breadcrumbs.Crumb>
			{/each}
		</Breadcrumbs>
		<div class="flex justify-end">
			<Button
				type="ghost"
				on:click={() => {
					isDropdownVisible = true;
				}}
				>{'+ Add Group'}
			</Button>
		</div>
	</Card.Header>

	<Card.Content slot="content" class="p-0 sm:p-0">
		<List>
			{#if isDropdownVisible}
				<div class="m-2 flex" transition:slide|local={{ duration: 500 }}>
					<Input
						class="w-[80%]"
						name="new-service-name"
						error={newGroupName.error}
						bind:value={newGroupName.value}
					></Input>

					<Button
						class="w-[10%]"
						on:click={() => {
							isDropdownVisible = false;
							newGroupName.value = undefined;
						}}>Cancel</Button
					>
					<Button class="w-[10%]" type="primary" on:click={handleAddNewGroup}>Add</Button>
				</div>
			{/if}

			{#each groups as group}
				<List.Item
					on:click={() => goto(`/groups/${group.id}`)}
					class="cursor-pointer hover:bg-slate-100"
				>
					<div class="mx-4">{group.title}</div>
				</List.Item>
			{/each}
		</List>
	</Card.Content>
</Card>
