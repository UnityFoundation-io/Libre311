<script lang="ts" context="module">
	let cachedServiceList: GetServiceListResponse | undefined = undefined;
</script>

<script lang="ts">
	import EllipsisVertical from '$lib/components/Svg/outline/EllipsisVertical.svelte';
	import ChevronRight from '$lib/components/Svg/outline/ChevronRight.svelte';

	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetServiceListResponse, Service } from '$lib/services/Libre311/Libre311';
	import type { SelectOption } from 'stwui/types';
	import { stringValidator, type FormInputValue, createInput } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Dropdown, Input, List, Modal, Portal } from 'stwui';
	import { onMount } from 'svelte';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { page } from '$app/stores';
	import { slide } from 'svelte/transition';
	import ToggleState from '$lib/components/ToggleState.svelte';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import CheckMark from '$lib/components/Svg/outline/CheckMark.svelte';

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
	let isDeleteModalOpen = false;
	let groupId = Number($page.params.group_id);
	let newServiceName: FormInputValue<string> = createInput();
	let serviceToDelete: Service;

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

	function handleEditButton(service: Service) {
		isEditServiceInputVisible = true;
		editServiceCode = service.service_code;
		editServiceName = service.service_name;
	}

	function handleDeleteButton(service: Service) {
		isDeleteModalOpen = true;
		serviceToDelete = service;
	}

	function closeDeleteServiceModal() {
		isDeleteModalOpen = false;
	}

	async function handleEditServiceButton(service: Service) {
		if (serviceList.type !== 'success') return;

		const res = await libre311.editService({
			service_code: service.service_code,
			service_name: editServiceName
		});

		isEditServiceInputVisible = false;

		let foundIndex = serviceList.value.findIndex((x) => x.service_code == res.service_code);
		serviceList.value[foundIndex] = res;
	}

	async function handleDeleteServiceButton() {
		if (serviceList.type !== 'success') return;

		isDeleteModalOpen = false;

		await libre311.deleteService({
			service_code: serviceToDelete.service_code
		});

		let foundIndex = serviceList.value.findIndex((x) => x.service_code == serviceToDelete.service_code);
		delete serviceList.value[foundIndex];
		serviceList = serviceList;
	}

	onMount(fetchServiceList);

	let isEditServiceInputVisible = false;
	let editServiceCode: number;
	let editServiceName: string;
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
					{#if service !== undefined}
						<List.Item class="flex cursor-pointer items-center hover:bg-slate-100">
							<div class="">
								<ToggleState startingValue={false} let:show let:toggle>
									<Dropdown visible={show}>
										<Button type="ghost" slot="trigger" on:click={toggle}>
											<EllipsisVertical slot="icon" />
										</Button>

										<Dropdown.Items slot="items" class="w-[100px]">
											<Button
												type="ghost"
												class="w-full"
												on:click={() => handleEditButton(service)}
											>
												Edit
											</Button>
											<Button
												type="ghost"
												class="w-full"
												on:click={() => handleDeleteButton(service)}
											>
												Delete
											</Button>
										</Dropdown.Items>
									</Dropdown>
								</ToggleState>
							</div>

							<div class="mx-4 w-full">
								{#if isEditServiceInputVisible && editServiceCode == service.service_code}
									<Input
										class="w-full"
										type="text"
										name="new-service-name"
										bind:value={editServiceName}
									></Input>
								{:else}
									{service?.service_name ?? ''}
								{/if}
							</div>

							<div class="">
								<div class="flex justify-end">
									<div class="mx-2 flex items-center justify-center">
										{#if isEditServiceInputVisible && editServiceCode == service.service_code}
											<Button
												aria-label="Close"
												type="ghost"
												on:click={() => {
													isEditServiceInputVisible = false;
												}}
											>
												<XMark slot="icon" />
											</Button>

											<Button
												aria-label="Submit"
												type="ghost"
												on:click={() => handleEditServiceButton(service)}
											>
												<CheckMark slot="icon" />
											</Button>
										{:else}
											<Button type="ghost" href={`/groups/1/services/${service.service_code}`}>
												<ChevronRight slot="icon" />
											</Button>
										{/if}
									</div>
								</div>
							</div>
						</List.Item>
					{/if}

					<Portal>
						{#if isDeleteModalOpen}
							<Modal handleClose={closeDeleteServiceModal}>
								<Modal.Content slot="content" class="max-h-full w-1/2">
									<Modal.Content.Body slot="body" class="overflow-y-auto">
										<div class="my-4 flex">
											<strong>Remove Service: &nbsp;</strong>
											{` ${serviceToDelete.service_name}`}
										</div>

										<div class="grid grid-cols-2 gap-2">
											<Button class="col-span-1" type="ghost" on:click={closeDeleteServiceModal}
												>Cancel</Button
											>
											<Button class="col-span-1" type="danger" on:click={handleDeleteServiceButton}
												>Confirm</Button
											>
										</div>
									</Modal.Content.Body>
								</Modal.Content>
							</Modal>
						{/if}
					</Portal>
				{/each}
			</List>
		</Card.Content>
	{:else if serviceList.type === 'failure'}
		{JSON.stringify(serviceList.error, null, 2)}
	{/if}
</Card>
