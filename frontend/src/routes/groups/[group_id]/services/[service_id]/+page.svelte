<script lang="ts">
	import { run } from 'svelte/legacy';

	import messages from '$media/messages.json';
	import { Breadcrumbs, Card, Input, List, Progress, Select } from 'stwui';
	import { Button } from '$lib/components/ui/button';
	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult
	} from '$lib/services/http';
	import {
		createAttributeInputMap,
		type AttributeInputMap,
		type ServiceDefinitionAttributeInputUnion
	} from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { slide } from 'svelte/transition';
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import type { SelectOption } from 'stwui/types';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import type { CreateServiceDefinitionAttributesParams } from '$lib/services/Libre311/Libre311';
	import SdaListItem from '$lib/components/ServiceDefinitionEditor/SdaListItem.svelte';
	import type { ComponentEvents } from 'svelte';
	import DragAndDrop from '$lib/components/DragAndDrop.svelte';

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

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = $state(ASYNC_IN_PROGRESS);

	let groupId = $page.params.group_id;
	let serviceCode = Number($page.params.service_id);
	let serviceId: number;

	let isNewAttributeDropDownVisable: boolean = $state(false);

	let newAttribute: AttributeInput = $state({
		description: createInput<string>(),
		dataTypeDescription: createInput<string>(),
		dataType: undefined,
		required: false,
		order: 0,
		values: undefined
	});

	let values: AttributeValue[] = $state([
		{
			id: 0,
			name: ''
		}
	]);

	let multivalueErrorMessage: string | undefined = $state();
	let dataTypeSelectError: string | undefined = $state();

	let groupName = $state('');
	let serviceName = $state('');



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

		// Data Type validation
		if (newAttribute.dataType == undefined) {
			dataTypeSelectError = 'Answer Type Required';
		} else {
			dataTypeSelectError = undefined;
		}

		// Return if any input errors
		if (serviceId == null) {
			return;
		}
		if (newAttribute.description.type != 'valid') {
			return;
		}
		if (newAttribute.dataTypeDescription.type != 'valid') {
			return;
		}
		if (dataTypeSelectError) {
			return;
		}

		try {
			const body: CreateServiceDefinitionAttributesParams = {
				service_code: serviceId,
				description: newAttribute.description.value,
				datatype_description: newAttribute.dataTypeDescription.value,
				datatype: String(newAttribute?.dataType).toString(),
				variable: true,
				required: newAttribute.required,
				order: newAttribute.order
			};

			if (newAttribute.dataType == 'multivaluelist') {
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

			// Reset inputs
			isNewAttributeDropDownVisable = false;
			newAttribute.description.value = '';
			newAttribute.dataTypeDescription.value = '';
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

	function removeFromAttributeMap(e: ComponentEvents<SdaListItem>['attributeDeleted']) {
		if (asyncAttributeInputMap.type != 'success') return;
		asyncAttributeInputMap.value.delete(e.detail.code);
		asyncAttributeInputMap = asyncAttributeInputMap;
	}

	async function updateAttributesOrder(
		e: ComponentEvents<DragAndDrop<ServiceDefinitionAttributeInputUnion>>['itemsChanged']
	) {
		if (asyncAttributeInputMap.type != 'success') return;

		try {
			const res = await libre311.updateAttributesOrder({
				service_code: serviceCode,
				attributes: e.detail.map((item, idx) => {
					return {
						code: item.attribute.code,
						order: idx
					};
				})
			});
			asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(res, {}));
		} catch (error: unknown) {
			alertError(error);
		}
	}
	let crumbs = $derived([
		{ label: `Group: ${groupName}`, href: '/groups' },
		{ label: `Service: ${serviceName}`, href: `/groups/${groupId}` },
		{ label: `Attributes`, href: `/groups/${groupId}/services/${serviceCode}` }
	]);
	run(() => {
		updateAttributeMap(serviceCode);
	});
</script>

<Card bordered={true} class="m-4">
	{#snippet header()}
		<Card.Header  class="flex items-center justify-between py-3 text-lg font-bold">
			<Breadcrumbs>
				{#each crumbs as crumb}
					<Breadcrumbs.Crumb href={crumb.href}>
						{#snippet label()}
										<Breadcrumbs.Crumb.Label ><h3>{crumb.label}</h3></Breadcrumbs.Crumb.Label>
									{/snippet}
					</Breadcrumbs.Crumb>
				{/each}
			</Breadcrumbs>
		</Card.Header>
	{/snippet}

	{#snippet content()}
		<Card.Content  class="p-0 sm:p-0">
			{#if asyncAttributeInputMap?.type === 'success'}
				<List>
					<DragAndDrop
						items={Array.from(asyncAttributeInputMap.value.values())}
						on:itemsChanged={updateAttributesOrder}
					>
						{#snippet item({ item })}
										<SdaListItem
								
								
								on:attributeDeleted={removeFromAttributeMap}
								groupId={Number($page.params.group_id)}
								serviceCode={Number($page.params.service_id)}
								sda={item.attribute}
							/>
									{/snippet}
					</DragAndDrop>

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
										error={dataTypeSelectError}
										bind:value={newAttribute.dataType}
									>
										<!-- @migration-task: migrate this slot by hand, `options` would shadow a prop on the parent component -->
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
										{#snippet label()}
																		<Input.Label >
												<strong class="text-base"
													>{messages['serviceDefinitionEditor']['attributes']['description']}</strong
												>
											</Input.Label>
																	{/snippet}
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
										{#snippet label()}
																		<Input.Label >
												<strong class="text-base"
													>{messages['serviceDefinitionEditor']['attributes'][
														'data_type_description'
													]}</strong
												>
											</Input.Label>
																	{/snippet}
									</Input>
								</div>

								{#if newAttribute.dataType == 'multivaluelist'}
									<div class="flex flex-col" transition:slide|local={{ duration: 500 }}>
										<strong class="text-base">{'Values:'}</strong>

										<ul>
											{#each values as value, index}
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
														bind:value={value.name}
													/>

													{#if index != 0}
														<Button on:click={() => removeValue(index)}>
															<XMark />
														</Button>
													{/if}
												</li>
											{/each}
										</ul>

										<Button class="mt-1" variant="ghost" on:click={addValue}>
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
								variant="ghost"
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
								variant="default"
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
								variant="ghost"
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
	{/snippet}
</Card>
