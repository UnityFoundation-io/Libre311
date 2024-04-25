<script lang="ts">
	import GroupListItem from '$lib/components/ServiceDefinitionEditor/GroupListItem.svelte';

	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetGroupListResponse } from '$lib/services/Libre311/Libre311';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Input, List } from 'stwui';
	import { onMount, type ComponentEvents } from 'svelte';
	import { slide } from 'svelte/transition';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [{ label: 'Groups', href: '/groups' }];

	const libre311 = useLibre311Service();

	let groupList: AsyncResult<GetGroupListResponse> = ASYNC_IN_PROGRESS;
	let isDropdownVisible = false;
	let newGroupName: FormInputValue<string> = createInput();

	function fetchGroupList() {
		libre311
			.getGroupList()
			.then((res) => {
				groupList = asAsyncSuccess(res);
			})
			.catch((err) => (groupList = asAsyncFailure(err)));
	}

	async function handleAddNewGroup() {
		if (groupList.type !== 'success') return;

		newGroupName = stringValidator(newGroupName);

		if (newGroupName.type != 'valid') {
			return;
		}
		try {
			const res = await libre311.createGroup({
				name: newGroupName.value
			});
			newGroupName.value = '';
			isDropdownVisible = false;
			groupList.value.unshift(res);
			groupList = groupList;
		} catch (error: unknown) {
			groupList = asAsyncFailure(error);
		}
	}

	async function updateGroupListState(e: ComponentEvents<GroupListItem>['editSuccess']) {
		if (groupList.type !== 'success') return;
		const updatedGroup = e.detail;
		let foundIndex = groupList.value.findIndex((x) => x.id == updatedGroup.id);
		groupList.value[foundIndex] = updatedGroup;
		groupList = groupList;
	}

	onMount(fetchGroupList);
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

	{#if groupList.type === 'success'}
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

				{#each groupList.value as group}
					<GroupListItem on:editSuccess={updateGroupListState} {group} />
				{/each}
			</List>
		</Card.Content>
	{:else if groupList.type === 'failure'}
		{JSON.stringify(groupList.error, null, 2)}
	{:else if groupList.type === 'inProgress'}
		<h2>Loading...</h2>
	{/if}
</Card>
