<script lang="ts">
	import { Breadcrumbs, Button, Card, Input, List, Progress } from 'stwui';
	import { asAsyncFailure, asAsyncSuccess, ASYNC_IN_PROGRESS, type AsyncResult } from '$lib/services/http';
	import { createAttributeInputMap, type AttributeInputMap } from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import CheckMark from '$lib/components/Svg/outline/CheckMark.svelte';
	import { slide } from 'svelte/transition';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = ASYNC_IN_PROGRESS;
	let selectedServiceCode: string = $page.params.service_id;
	let isDropDownVisable: boolean = false;
	let newAttributeName: FormInputValue<string> = createInput();

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: '/groups/1/' },
		{ label: 'Attributes', href: '/groups/1/services/1' }
	];

	$: updateAttributeMap(selectedServiceCode);

	function updateAttributeMap(service: string) {
		if (!service) {
			return;
		}
		getServiceDefinition(service);
	}

	async function getServiceDefinition(selectedServiceCode: string) {
		try {
			const payload = { service_code: selectedServiceCode };
			const res = await libre311.getServiceDefinition(payload);
			asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(res, {}));
		} catch (error) {
			asyncAttributeInputMap = asAsyncFailure(error);
			alertError(error);
		}
	}

	async function handleAddNewAttribute() {
		console.log("HANDLE NEW ATTRIBUTE");
		console.log(newAttributeName);

		newAttributeName = stringValidator(newAttributeName);

		if (newAttributeName.type != 'valid') {
			return;
		}

		try {
			const res = await libre311.createAttribute({
				serviceId: 1,
				description: 'I am an attribute'
			});
			console.log('RES')
			console.log(res);
		} catch (error) {
			alertError(error);
		}

		isDropDownVisable = false;
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
					isDropDownVisable = true;
				}}
				>{'+ Add Attribute'}
			</Button>
		</div>
	</Card.Header>

	<Card.Content slot="content" class="p-0 sm:p-0">

		{#if asyncAttributeInputMap?.type === 'success'}
			<List>
				{#if isDropDownVisable}
					<div class="m-2 w-full flex justify-between" transition:slide|local={{ duration: 500 }}>
						<Input
							class="w-[80%]"
							name="new-service-name"
							error={newAttributeName.error}
							bind:value={newAttributeName.value}
						></Input>

						<div class="flex">
							<Button
								aria-label="Close"
								type="ghost"
								on:click={() => {
									isDropDownVisable = false;
									newAttributeName.value = undefined;
								}}
							>
								<XMark slot="icon" />
							</Button>
							<Button aria-label="Submit" type="ghost" on:click={handleAddNewAttribute}>
								<CheckMark slot="icon" />
							</Button>
						</div>
					</div>
				{/if}

				{#each asyncAttributeInputMap.value.values() as input}
					<List.Item class="flex items-center cursor-pointer hover:bg-slate-100">

						<div class="mx-4 w-full">
							{input.attribute.description}
						</div>

					</List.Item>
				{/each}
			</List>
		{:else if asyncAttributeInputMap?.type === 'inProgress'}
			<div class="mx-8 my-4">
				<Progress value={0} indeterminate />
			</div>
		{/if}
	</Card.Content>
</Card>
