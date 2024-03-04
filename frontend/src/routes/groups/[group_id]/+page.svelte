<script lang="ts" context="module">
	let cachedServiceList: GetServiceListResponse | undefined = undefined;
</script>

<script lang="ts">
	import { goto } from '$app/navigation';

	import { ellipsisSVG } from '$lib/components/Svg/outline/EllipsisVertical.svelte';

	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetServiceListResponse } from '$lib/services/Libre311/Libre311';
	import type { SelectOption } from 'stwui/types';
	import { stringValidator, type FormInputValue, createInput } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Dropdown, Input, List } from 'stwui';
	import { JackList } from '$lib/components/JackList';
	import { onMount } from 'svelte';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { page } from '$app/stores';
	import { slide } from 'svelte/transition';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: '/groups/1/' }
	];

	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let isDropDownVisable = false;
	let groupId = Number($page.params.group_id);
	let newServiceName: FormInputValue<string> = createInput();

	let visible1 = false;

	function toggleDropdown1() {
		visible1 = !visible1;
	}

	function fetchServiceList() {
		if (cachedServiceList) {
			serviceList = asAsyncSuccess(cachedServiceList);
			return;
		}
		libre311
			.getServiceList()
			.then((res) => {
				cachedServiceList = res;
				serviceList = asAsyncSuccess(res);
			})
			.catch((err) => (serviceList = asAsyncFailure(err)));
	}

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	async function handleAddNewService() {
		if (serviceList.type !== 'success') {
			return;
		}

		newServiceName = stringValidator(newServiceName);

		if (newServiceName.type != 'valid') {
			return;
		}

		const res = await libre311.createService({
			service_name: newServiceName.value,
			group_id: groupId
		});

		newServiceName.value = '';
		isDropDownVisable = false;
		serviceList.value.unshift(res);
		serviceList = serviceList;
	}

	onMount(fetchServiceList);
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
					isDropDownVisable = true;
				}}
				>{'+ Add Service'}
			</Button>
		</div>
	</Card.Header>

	{#if serviceList.type === 'success'}
		<Card.Content slot="content" class="p-0 sm:p-0">
			{@const selectOptions = createSelectOptions(serviceList.value)}
			<List>
				{#if isDropDownVisable}
					<div class="m-2 flex" transition:slide|local={{ duration: 500 }}>
						<Input
							class="w-[80%]"
							name="new-service-name"
							error={newServiceName.error}
							bind:value={newServiceName.value}
						></Input>

						<Button
							class="w-[10%]"
							on:click={() => {
								isDropDownVisable = false;
								newServiceName.value = undefined;
							}}>Cancel</Button
						>
						<Button class="w-[10%]" type="primary" on:click={handleAddNewService}>Add</Button>
					</div>
				{/if}

				{#each selectOptions as service}
					<List.Item>
						<List.Item.Content
							class="cursor-pointer hover:bg-slate-100"
							slot="content"
							on:click={() => goto(`/groups/1/services/${service.value}`)}
						>
							<List.Item.Content.Title slot="title" class="mx-4">
								{service.label}
							</List.Item.Content.Title>
						</List.Item.Content>

						<List.Item.Extra slot="extra" placement="start">
							<Dropdown bind:visible={visible1}>
								<Button slot="trigger" type="ghost" shape="circle" on:click={toggleDropdown1}>
									<Button.Icon data={ellipsisSVG} />
								</Button>

								<Dropdown.Items slot="items" class="w-[100px]">
									<Button type="ghost" class="w-full">Edit</Button>
								</Dropdown.Items>
							</Dropdown>
						</List.Item.Extra>
					</List.Item>
				{/each}
			</List>
		</Card.Content>
	{:else if serviceList.type === 'failure'}
		{JSON.stringify(serviceList.error, null, 2)}
	{/if}
</Card>

<hr style="border: 2px solid red;margin: 0.375rem;" />

<section class="m-4">
	{#if serviceList.type === 'success'}
		{@const selectOptions = createSelectOptions(serviceList.value)}
		<JackList items={selectOptions}>
			<JackList.Item></JackList.Item>
		</JackList>
	{/if}
</section>
