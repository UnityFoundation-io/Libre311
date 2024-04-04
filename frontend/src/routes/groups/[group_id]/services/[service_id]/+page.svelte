<script lang="ts">
	import messages from '$media/messages.json';
	import { Breadcrumbs, Button, Card, Input, List, Progress, Select } from 'stwui';
	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult
	} from '$lib/services/http';
	import {
		createAttributeInputMap,
		type AttributeInputMap
	} from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { slide } from 'svelte/transition';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import type { SelectOption } from 'stwui/types';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import type { CreateServiceDefinitionAttributesParams } from '$lib/services/Libre311/Libre311';

	type AttributeValue = {
		id: number;
		name: string;
	};

	type AttributeInput = {
		description: FormInputValue<string>;
		dataTypeDescription: FormInputValue<string>;
		dataType: string | undefined;
		required: boolean;
		order: number;
		values: AttributeValue[] | undefined;
	};

	interface Crumb {
		label: string;
		href: string;
	}

	const dataTypeOptions: SelectOption[] = [
		{
			value: 'string',
			label: 'Single Answer'
		},
		{
			value: 'multivaluelist',
			label: 'Multi-Answer'
		}
	];

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = ASYNC_IN_PROGRESS;

	let groupId = $page.params.group_id;
	let serviceCode = Number($page.params.service_id);
	let serviceId: number;

	let isNewAttributeDropDownVisable: boolean = false;

	let newAttribute: AttributeInput = {
		description: createInput<string>(''),
		dataTypeDescription: createInput<string>(''),
		dataType: undefined,
		required: false,
		order: 0,
		values: undefined
	};

	let values: AttributeValue[] = [
		{
			id: 0,
			name: ''
		}
	];

	let multivalueErrorMessage: string | undefined;

	let groupName = '';
	let serviceName = '';

	$: crumbs = [
		{ label: `Group: ${groupName}`, href: '/groups' },
		{ label: `Service: ${serviceName}`, href: `/groups/${groupId}` },
		{ label: `Attributes`, href: `/groups/${groupId}/services/${serviceCode}` }
	];

	$: updateAttributeMap(serviceCode);

	function updateAttributeMap(service: number) {
		if (!service) {
			return;
		}
		getServiceDefinition(service);
	}

	async function getServiceDefinition(serviceCode: number) {
		try {
			// Get Group
			const groups = await libre311.getGroupList();

			const group = groups.find((group) => group.id === Number(groupId));
			if (group) {
				groupName = group.name;
			}

			// Get Service Definition
			const payload = { service_code: serviceCode };
			const res = await libre311.getServiceDefinition(payload);
			const attributes = res.attributes;

			// Get order
			newAttribute.order = attributes.length;

			// Get Service ID
			const serviceList = await libre311.getServiceList();
			for (let service of serviceList) {
				if (service.service_code == serviceCode) {
					serviceId = service.service_code;
					serviceName = service.service_name;
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
		newAttribute.dataTypeDescription = stringValidator(newAttribute.dataTypeDescription);

		if (serviceId == null) {
			return;
		}
		if (newAttribute.description.type != 'valid') {
			return;
		}
		if (newAttribute.dataTypeDescription.type != 'valid') {
			return;
		}

		try {
			const body: CreateServiceDefinitionAttributesParams = {
				serviceId: serviceId,
				description: newAttribute.description.value,
				datatype_description: newAttribute.dataTypeDescription.value,
				datatype: String(newAttribute?.dataType).toString(),
				variable: true,
				required: newAttribute.required,
				order: newAttribute.order
			};

			if (values && values.length > 0) {
				const valueArray = values.map((v) => {
					return {
						key: v.id.toString(),
						name: v.name
					};
				});
				if (valueArray[0].name == '') {
					multivalueErrorMessage = 'You might want to add a value!';
					return;
				} else {
					multivalueErrorMessage = undefined;
					body.values = valueArray;
				}
			}

			await libre311.createAttribute(body);

			isNewAttributeDropDownVisable = false;
			newAttribute.description.value = '';
			newAttribute.required = false;
			newAttribute.dataType = undefined;
			values = [{ id: 0, name: '' }];
			updateAttributeMap(serviceCode);
		} catch (error) {
			alertError(error);
		}
	}

	function addValue() {
		const newId = values?.length ?? 1;
		values = [...values, { id: newId, name: '' }];
		multivalueErrorMessage = undefined;
	}

	function removeValue(index: number) {
		values = values.filter((_, i) => i !== index);
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
	</Card.Header>

	<Card.Content slot="content" class="p-0 sm:p-0">
		{#if asyncAttributeInputMap?.type === 'success'}
			<List>
				{#each asyncAttributeInputMap.value.values() as input}
					<List.Item class="flex cursor-pointer items-center hover:bg-slate-100">
						<div class="mx-4 w-full">
							{input.attribute.description}
						</div>
					</List.Item>
				{/each}

				{#if isNewAttributeDropDownVisable}
					<div
						class="flex w-full flex-col justify-between"
						transition:slide|local={{ duration: 500 }}
					>
						<div class="mx-4 my-2">
							<div class="my-2 flex items-center justify-between">
								<div class="items-center">
									<label for="is-attribute-required">
										<strong class="text-base"
											>{messages['serviceDefinitionEditor']['attributes']['required']}</strong
										>
									</label>
									<input
										class="mx-2 rounded-sm"
										id="is-attribute-required"
										type="checkbox"
										bind:checked={newAttribute.required}
									/>
								</div>

								<Select
									name="select-datatype"
									placeholder={messages['serviceDefinitionEditor']['attributes'][
										'select_data_type_placeholder'
									]}
									options={dataTypeOptions}
									bind:value={newAttribute.dataType}
								>
									<Select.Options slot="options">
										{#each dataTypeOptions as option}
											<Select.Options.Option {option} />
										{/each}
									</Select.Options>
								</Select>
							</div>

							<div class="my-4">
								<Input
									name="new-attribute-description"
									error={newAttribute.description.error}
									bind:value={newAttribute.description.value}
									placeholder={messages['serviceDefinitionEditor']['attributes'][
										'description_placeholder'
									]}
								>
									<Input.Label slot="label">
										<strong class="text-base"
											>{messages['serviceDefinitionEditor']['attributes']['description']}</strong
										>
									</Input.Label>
								</Input>
							</div>

							<div class="my-4">
								<Input
									name="new-attribute-datatype-description"
									error={newAttribute.dataTypeDescription.error}
									bind:value={newAttribute.dataTypeDescription.value}
									placeholder={messages['serviceDefinitionEditor']['attributes'][
										'data_type_description_placeholder'
									]}
								>
									<Input.Label slot="label">
										<strong class="text-base"
											>{messages['serviceDefinitionEditor']['attributes'][
												'data_type_description'
											]}</strong
										>
									</Input.Label>
								</Input>
							</div>

							{#if newAttribute.dataType == 'multivaluelist'}
								<div class="flex flex-col" transition:slide|local={{ duration: 500 }}>
									<strong class="text-base">{'Values:'}</strong>

									<ul>
										{#each values as _, index}
											<li
												class="my-2 flex justify-between"
												transition:slide|local={{ duration: 500 }}
											>
												<Input
													class="w-11/12 rounded-md"
													type="text"
													placeholder={messages['serviceDefinitionEditor']['attributes'][
														'value_placeholder'
													]}
													error={multivalueErrorMessage}
													bind:value={values[index].name}
												/>

												{#if index != 0}
													<Button on:click={() => removeValue(index)}>
														<XMark />
													</Button>
												{/if}
											</li>
										{/each}
									</ul>

									<Button class="mt-1" type="ghost" on:click={addValue}>
										{'+ Add'}
									</Button>
								</div>
							{/if}
						</div>
					</div>
				{/if}

				{#if isNewAttributeDropDownVisable}
					<List.Item class="flex h-[3.5rem] items-center justify-between">
						<Button
							class="mx-2 w-1/2"
							aria-label="Close"
							type="ghost"
							on:click={() => {
								isNewAttributeDropDownVisable = false;
								newAttribute.description.value = undefined;
								newAttribute.dataTypeDescription.value = undefined;
								newAttribute.dataType = undefined;
								newAttribute.required = false;
								values = [{ id: 0, name: '' }];
								multivalueErrorMessage = undefined;
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
							{'Save Attribute'}
						</Button>
					</List.Item>
				{:else}
					<List.Item class="flex h-[3.5rem] items-center justify-end">
						<Button
							class="mr-2"
							aria-label="Add"
							type="ghost"
							on:click={() => {
								isNewAttributeDropDownVisable = true;
							}}
						>
							{messages['serviceDefinitionEditor']['attributes']['add_attribute']}
						</Button>
					</List.Item>
				{/if}
			</List>
		{:else if asyncAttributeInputMap?.type === 'inProgress'}
			<div class="mx-8 my-4">
				<Progress value={0} indeterminate />
			</div>
		{/if}
	</Card.Content>
</Card>
