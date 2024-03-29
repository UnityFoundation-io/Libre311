<script lang="ts">
	import { Breadcrumbs, Button, Card, Input, List, Progress, Select } from 'stwui';
	import { asAsyncFailure, asAsyncSuccess, ASYNC_IN_PROGRESS, type AsyncResult } from '$lib/services/http';
	import { createAttributeInputMap, type AttributeInputMap } from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { slide } from 'svelte/transition';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import type { SelectOption } from 'stwui/types';

	type AttributeInput = {
		description: FormInputValue<string>,
		dataType: string | undefined,
		required: boolean,
		order: number
	}

	interface Crumb {
		label: string;
		href: string;
	}

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = ASYNC_IN_PROGRESS;

	let groupId = $page.params.group_id;
	let serviceCode = $page.params.service_id;
	let serviceId: number;

	let isDropDownVisable: boolean = false;

	let newAttribute: AttributeInput = {
		description: createInput<string>(''),
		code: createInput<string>(''),
		dataType: undefined,
		required: false,
		order: 0
	};

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: `/groups/${groupId}` },
		{ label: 'Attributes', href: `/groups/${groupId}/services/${serviceCode}` }
	];

	const dataTypeOptions: SelectOption[] = [
		{
			value: 'string',
			label: 'String'
		},
		{
			value: 'multivaluelist',
			label: 'Multi-Value'
		}
	];

	$: updateAttributeMap(serviceCode);

	function updateAttributeMap(service: string) {
		if (!service) {
			return;
		}
		getServiceDefinition(service);
	}

	async function getServiceDefinition(serviceCode: string) {
		try {
			const payload = { service_code: serviceCode };
			const res = await libre311.getServiceDefinition(payload);
			const attributes = res.attributes;

			// Get order
			newAttribute.order = attributes[attributes.length - 1].order + 1;

			// Get Service ID
			const serviceList = await libre311.getServiceList();
			for (let service of serviceList) {
				if (service.service_code == serviceCode) {
					serviceId = service.id;
				}
			}

			asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(res, {}));
		} catch (error) {
			asyncAttributeInputMap = asAsyncFailure(error);
			alertError(error);
		}
	}

	async function handleAddNewAttribute() {
		newAttribute.description = stringValidator(newAttribute.description);

		if (serviceId == null) {
			return;
		}
		if (newAttribute.description.type != 'valid') {
			return;
		}

		try {
			await libre311.createAttribute({
				serviceId: serviceId,
				description: newAttribute.description.value,
				datatype: String(newAttribute?.dataType).toString(),
				variable: true,
				required: newAttribute.required,
				order: newAttribute.order
			});

			isDropDownVisable = false;
			newAttribute.description.value = '';
			newAttribute.required = false;
			newAttribute.dataType = undefined;
			updateAttributeMap(serviceCode);
		} catch (error) {
			alertError(error);
		}
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
					<div class="w-full flex flex-col justify-between" transition:slide|local={{ duration: 500 }}>
						<div class="my-2 mx-4">
							<div class="my-4 flex justify-between items-center">
								<div class="items-center">
									<label for="is-attribute-required">
										<strong class="text-base">{'Required:'}</strong>
									</label>
									<input class="rounded-sm mx-2" id="is-attribute-required" type="checkbox" bind:checked={newAttribute.required}/>
								</div>

								<Select
									name="select-datatype"
									placeholder="Select Data Type"
									options={dataTypeOptions}
									bind:value={newAttribute.dataType}
								>
									<Select.Options slot="options">
										{#each dataTypeOptions as option}
											<Select.Options.Option {option}/>
										{/each}
									</Select.Options>
								</Select>
							</div>

							<div class="my-4">
								<Input
									name="new-attribute-description"
									error={newAttribute.description.error}
									bind:value={newAttribute.description.value}
									placeholder="Tell us what is wrong with ...?"
								>
									<Input.Label slot="label">
										<strong class="text-base">{'Question:'}</strong>
									</Input.Label>
								</Input>
							</div>

							<div class="flex items-end my-4">
								<Button
									class="mx-2 w-1/2"
									aria-label="Close"
									type="ghost"
									on:click={() => {
										isDropDownVisable = false;
									}}
								>
									{'Cancel'}
								</Button>

								<Button
									class="mx-2 w-1/2"
									aria-label="Submit"
									type="primary"
									on:click={handleAddNewAttribute}
								>
									{'Submit'}
								</Button>
							</div>

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
