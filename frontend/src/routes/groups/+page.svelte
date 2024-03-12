<script lang="ts" context="module">
	let cachedGroupList: GetGroupListResponse | undefined = undefined;
</script>

<script lang="ts">
	import { goto } from '$app/navigation';
	import { checkMark } from '$lib/components/Svg/outline/CheckMark.svelte';
	import { chevronRightSvg } from '$lib/components/Svg/outline/ChevronRight.svelte';
	import { ellipsisSVG } from '$lib/components/Svg/outline/EllipsisVertical.svelte';
	import { xMark } from '$lib/components/Svg/outline/XMark';
	import ToggleState from '$lib/components/ToggleState.svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetGroupListResponse, Group } from '$lib/services/Libre311/Libre311';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Input, List, Dropdown } from 'stwui';
	import { onMount } from 'svelte';
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
	let isEditGroupInputVisible: boolean = false;
	let editGroupId: number;
	let editGroupName: FormInputValue<string> = createInput();

	function fetchGroupList() {
		if (cachedGroupList) {
			groupList = asAsyncSuccess(cachedGroupList);
		}
		libre311
			.getGroupList()
			.then((res) => {
				cachedGroupList = res;
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

	function handleEditButton(group: Group) {
		isEditGroupInputVisible = true;
		editGroupId = group.id;
		editGroupName.value = group.name;
	}

	async function handleEditGroupButton(group: Group) {
		if (groupList.type !== 'success') return;

		editGroupName = stringValidator(editGroupName);

		const res = await libre311.editGroup({
			id: group.id,
			name: String(editGroupName.value)
		});

		isEditGroupInputVisible = false;

		let foundIndex = groupList.value.findIndex((x) => x.id == res.id);
		groupList.value[foundIndex] = res;
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
					<List.Item class="flex cursor-pointer items-center hover:bg-slate-100">
						<div>
							<ToggleState startingValue={false} let:show let:toggle>
								<Dropdown visible={show}>
									<Button type="ghost" slot="trigger" on:click={toggle}>
										<Button.Icon slot="icon" type="ghost" data={ellipsisSVG} />
									</Button>

									<Dropdown.Items slot="items" class="w-[100px]">
										<Button type="ghost" class="w-full" on:click={() => handleEditButton(group)}>
											Edit
										</Button>
									</Dropdown.Items>
								</Dropdown>
							</ToggleState>
						</div>

						<div class="mx-4 w-full">
							{#if isEditGroupInputVisible && editGroupId == group.id}
								<Input
									class="w-full"
									type="text"
									name="new-service-name"
									bind:value={editGroupName.value}
								></Input>
							{:else}
								{group?.name ?? ''}
							{/if}
						</div>

						<div class="flex justify-end">
							<div class="mx-2 flex items-center justify-center">
								{#if isEditGroupInputVisible && editGroupId == group.id}
									<Button
										aria-label="Close"
										type="ghost"
										on:click={() => {
											isEditGroupInputVisible = false;
										}}
									>
										<Button.Icon slot="icon" type="ghost" data={xMark} fill="red" stroke="red" />
									</Button>

									<Button
										aria-label="Submit"
										type="ghost"
										on:click={() => handleEditGroupButton(group)}
									>
										<Button.Icon
											slot="icon"
											type="ghost"
											data={checkMark}
											fill="none"
											stroke="green"
										/>
									</Button>
								{:else}
									<Button type="ghost" href={`/groups/${group.id}`}>
										<Button.Icon data={chevronRightSvg} slot="icon" type="ghost"></Button.Icon>
									</Button>
								{/if}
							</div>
						</div>
					</List.Item>
				{/each}
			</List>
		</Card.Content>
	{:else if groupList.type === 'failure'}
		{JSON.stringify(groupList.error, null, 2)}
	{:else if groupList.type === 'inProgress'}
		<h2>Loading...</h2>
	{/if}
</Card>
