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
		return res.map((s) => ({ id: s.id, value: s.service_code, label: s.service_name }));
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

	function handleEditButton(value: number) {
		isContentDropDownVisable = true;
		isIconDropDownVisable = false;

		selectedServiceValue = value;
	}

	async function handleEditServiceButton(id: number, groupId: number, newServiceName: string) {
		const res = await libre311.editService({
			id: id,
			service_name: newServiceName,
			group_id: groupId
		});

		isContentDropDownVisable = false;
		// console.log('RES:', res);
		// console.log('Service List:', serviceList);

		let foundIndex = serviceList.value.findIndex((x) => x.id == res.id);
		serviceList.value[foundIndex] = res;
	}

	onMount(fetchServiceList);

	let isContentDropDownVisable = false;
	let isIconDropDownVisable = false;

	let selectedServiceValue: number;
	let editServiceName = '';
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

			<JackList>
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
					<JackList.Item isActive={false}>
						<div slot="content">
							<div class="flex items-center justify-between">
								{#if isContentDropDownVisable && selectedServiceValue == service.value}
									<div class="m-2 flex">
										<Input class="w-[80%]" name="new-service-name" bind:value={editServiceName}
										></Input>

										<Button
											class="w-[10%]"
											on:click={() => {
												isContentDropDownVisable = false;
												editServiceName = '';
											}}
										>
											Cancel
										</Button>
										<Button
											class="w-[10%]"
											type="primary"
											on:click={handleEditServiceButton(
												service.id,
												service.groupId,
												editServiceName
											)}>Submit</Button
										>
									</div>
								{:else}
									<div class="m-2">
										{service.label}
									</div>
								{/if}

								<div class="dropdown">
									<Button
										slot="trigger"
										type="ghost"
										shape="circle"
										on:click={() => {
											isIconDropDownVisable = !isIconDropDownVisable;
										}}
									>
										<Button.Icon data={ellipsisSVG} />
									</Button>

									<div
										style:visibility={isIconDropDownVisable ? 'visible' : 'hidden'}
										class="dropdown-left menu bg-base-100 rounded-box w-52 p-2 shadow"
									>
										<Button type="ghost" class="w-full" on:click={handleEditButton(service.value)}
											>Edit {service.value}</Button
										>
									</div>
								</div>
							</div>
						</div>
					</JackList.Item>
				{/each}
			</JackList>

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

<style>
	.dropdown {
		display: inline-block;
		position: relative;
	}
	.dropdown > :focus {
		outline-offset: 2px;
		outline: 2px solid #0000;
	}
	.dropdown .dropdown-content {
		visibility: hidden;
		z-index: 50;
		opacity: 0;
		transform-origin: top;
		--tw-scale-x: 0.95;
		--tw-scale-y: 0.95;
		transform: translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate))
			skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x))
			scaleY(var(--tw-scale-y));
		transition-property:
			color,
			background-color,
			border-color,
			-webkit-text-decoration-color,
			text-decoration-color,
			fill,
			stroke,
			opacity,
			box-shadow,
			transform,
			filter,
			backdrop-filter,
			-webkit-text-decoration-color,
			-webkit-backdrop-filter;
		transition-duration: 0.2s;
		transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
		position: absolute;
	}
	.dropdown-end .dropdown-content {
		right: 0;
	}
	.dropdown-left .dropdown-content {
		transform-origin: 100%;
		top: 0;
		bottom: auto;
		right: 100%;
	}
	.dropdown-right .dropdown-content {
		transform-origin: 0;
		top: 0;
		bottom: auto;
		left: 100%;
	}
	.dropdown-top .dropdown-content {
		transform-origin: bottom;
		top: auto;
		bottom: 100%;
	}
	.dropdown-end.dropdown-right .dropdown-content,
	.dropdown-end.dropdown-left .dropdown-content {
		top: auto;
		bottom: 0;
	}
	.dropdown.dropdown-open .dropdown-content,
	.dropdown.dropdown-hover:hover .dropdown-content,
	.dropdown:not(.dropdown-hover):focus .dropdown-content,
	.dropdown:not(.dropdown-hover):focus-within .dropdown-content {
		visibility: visible;
		opacity: 1;
	}
</style>
