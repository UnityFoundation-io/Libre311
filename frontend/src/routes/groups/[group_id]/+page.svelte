<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetServiceListResponse } from '$lib/services/Libre311/Libre311';
	import { stringValidator, type FormInputValue, createInput } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Input, List } from 'stwui';
	import { onMount, type ComponentEvents } from 'svelte';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { page } from '$app/stores';
	import { slide } from 'svelte/transition';

	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import CheckMark from '$lib/components/Svg/outline/CheckMark.svelte';
	import ServiceListItem from '$lib/components/ServiceDefinitionEditor/ServiceListItem.svelte';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: `/groups/${$page.params.group_id}` }
	];

	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let isDropDownVisable = false;
	let groupId = Number($page.params.group_id);
	let newServiceName: FormInputValue<string> = createInput();

	function fetchServiceList() {
		libre311
			.getServiceList()
			.then((res) => {
				// todo later use the proper endpoint to get the services for a particular group or create a context for this entire page heirarchy and provide method
				serviceList = asAsyncSuccess(res.filter((s) => s.group_id == groupId));
			})
			.catch((err) => (serviceList = asAsyncFailure(err)));
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

	async function handleServiceEdited(e: ComponentEvents<ServiceListItem>['serviceEdited']) {
		if (serviceList.type !== 'success') return;
		const idx = serviceList.value.findIndex((s) => s.service_code == e.detail.service_code);
		serviceList.value[idx] = e.detail;
	}

	function removeServiceFromState(e: ComponentEvents<ServiceListItem>['serviceDeleted']) {
		if (serviceList.type !== 'success') return;
		serviceList.value = serviceList.value.filter((s) => s.service_code != e.detail.service_code);
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
			<List>
				{#if isDropDownVisable}
					<div class="m-2 flex justify-between" transition:slide|local={{ duration: 500 }}>
						<Input
							class="w-[80%]"
							name="new-service-name"
							error={newServiceName.error}
							bind:value={newServiceName.value}
						></Input>

						<div class="flex">
							<Button
								aria-label="Close"
								type="ghost"
								on:click={() => {
									isDropDownVisable = false;
									newServiceName.value = undefined;
								}}
							>
								<XMark slot="icon" />
							</Button>

							<Button aria-label="Submit" type="ghost" on:click={handleAddNewService}>
								<CheckMark slot="icon" />
							</Button>
						</div>
					</div>
				{/if}

				{#each serviceList.value as service}
					<ServiceListItem
						on:serviceEdited={handleServiceEdited}
						on:serviceDeleted={removeServiceFromState}
						{service}
					/>
				{/each}
			</List>
		</Card.Content>
	{:else if serviceList.type === 'failure'}
		{JSON.stringify(serviceList.error, null, 2)}
	{/if}
</Card>
