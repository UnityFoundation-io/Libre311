<script lang="ts" context="module">
	let cachedServiceList: GetServiceListResponse | undefined = undefined;
</script>

<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetServiceListResponse } from '$lib/services/Libre311/Libre311';
	import type { SelectOption } from 'stwui/types';
	import { stringValidator, type FormInputValue, createInput } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Input, List, Modal, Portal } from 'stwui';
	import { onMount } from 'svelte';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { page } from '$app/stores';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: '/groups/1/' }
	];

	const libre311 = useLibre311Service();

	async function handleAddNewService() {
		newServiceName = stringValidator(newServiceName);

		if (newServiceName.type != 'valid') {
			return;
		}

		console.log(
			await libre311.createService({
				service_name: newServiceName.value,
				group_id: groupId
			})
		);

		isAddServiceModalOpen = false;
	}

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;

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

	onMount(fetchServiceList);

	let groupId = Number($page.params.group_id);
	let isAddServiceModalOpen: boolean = false;

	let newServiceName: FormInputValue<string> = createInput();
</script>

<Portal>
	{#if isAddServiceModalOpen}
		<Modal
			handleClose={() => {
				isAddServiceModalOpen = false;
			}}
		>
			<Modal.Content slot="content" class="max-h-full">
				<Modal.Content.Header slot="header" class="h-16">
					<h1 class="text-lg">Add A New Service</h1>
				</Modal.Content.Header>
				<Modal.Content.Body slot="body" class="overflow-y-auto">
					<Input
						class="m-2"
						name="new-service-name"
						error={newServiceName.error}
						bind:value={newServiceName.value}
					>
						<Input.Label slot="label">Name:</Input.Label>
					</Input>
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex items-center justify-center">
						<Button
							class="m-1 w-1/2"
							on:click={() => {
								isAddServiceModalOpen = false;
							}}>Cancel</Button
						>
						<Button class="m-1 w-1/2" type="primary" on:click={handleAddNewService}>Add</Button>
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	{/if}
</Portal>

<div class="m-4 flex justify-end">
	<Button
		type="ghost"
		on:click={() => {
			isAddServiceModalOpen = true;
		}}>{'+ Add Service'}</Button
	>
</div>

<Card bordered={true} class="m-4">
	<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
		<Breadcrumbs>
			{#each crumbs as crumb}
				<Breadcrumbs.Crumb href={crumb.href}>
					<Breadcrumbs.Crumb.Label slot="label"><h3>{crumb.label}</h3></Breadcrumbs.Crumb.Label>
				</Breadcrumbs.Crumb>
			{/each}
		</Breadcrumbs>
	</Card.Header>
	{#if serviceList.type === 'success'}
		<Card.Content slot="content" class="p-0 sm:p-0">
			{@const selectOptions = createSelectOptions(serviceList.value)}
			<List>
				{#each selectOptions as service}
					<List.Item
						on:click={() => goto(`/groups/1/services/${service.value}`)}
						class="cursor-pointer hover:bg-slate-100"
					>
						<div class="mx-4">{service.label}</div></List.Item
					>
				{/each}
			</List>
		</Card.Content>
	{:else if serviceList.type === 'failure'}
		{JSON.stringify(serviceList.error, null, 2)}
	{/if}
</Card>
