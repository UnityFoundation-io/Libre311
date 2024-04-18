<script lang="ts">
	import messages from '$media/messages.json';
	import { page } from "$app/stores";
	import { createAttributeInputMap, type AttributeInputMap } from "$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared";
	import { useLibre311Context, useLibre311Service } from "$lib/context/Libre311Context";
	import { ASYNC_IN_PROGRESS, asAsyncFailure, type AsyncResult, asAsyncSuccess } from "$lib/services/http";
	import { createInput, stringValidator, type FormInputValue } from "$lib/utils/validation";
	import { Breadcrumbs, Button, Card, Input, Progress } from "stwui";
	import { fade, slide } from "svelte/transition";
	import type { EditServiceDefinitionAttributeParams } from '$lib/services/Libre311/Libre311';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import { goto } from '$app/navigation';

	type AttributeEditValue = { key: string, name: string };

	type AttributeEditInput = {
		code: number;
		required: boolean;
		description: FormInputValue<string>;
		dataTypeDescription: FormInputValue<string>;
		values: AttributeEditValue[] | undefined;
	};

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = ASYNC_IN_PROGRESS;
	let groupId = $page.params.group_id;
	let serviceCode = Number($page.params.service_id);
	let attributeCode = Number($page.params.attribute_id);
	let groupName = '';
	let serviceName = '';
	let editAttribute: AttributeEditInput = {
		code: 0,
		required: false,
		description: createInput<string>(),
		dataTypeDescription: createInput<string>(),
		values: undefined
	};
	let multivalueErrorMessage: string | undefined;

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
			const serviceDefinition = await libre311.getServiceDefinition(payload);

			for (let attribute of serviceDefinition.attributes) {
				if (attribute.code == attributeCode) {
					editAttribute.code = attribute.code;
					editAttribute.required = attribute.required;
					editAttribute.description.value = attribute.description;
					editAttribute.dataTypeDescription.value = attribute.datatype_description?.toString();
					if (attribute.values) editAttribute.values = attribute.values;
					if (editAttribute.values) {
						editAttribute.values.sort((a, b) => Number(a.key) - Number(b.key));
					}
				}
			}

			// Get Service ID
			const serviceList = await libre311.getServiceList();
			for (let service of serviceList) {
				if (service.service_code == serviceCode) {
					serviceName = service.service_name;
				}
			}

			asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(serviceDefinition, {}));
		} catch (error) {
			asyncAttributeInputMap = asAsyncFailure(error);
			alertError(error);
		}
	}

	async function handleEditAttribute() {
		editAttribute.description = stringValidator(editAttribute.description);
		editAttribute.dataTypeDescription = stringValidator(editAttribute.dataTypeDescription);

		// Return if any input errors
		if (editAttribute.description.type != 'valid') {
			return;
		}
		if (editAttribute.dataTypeDescription.type != 'valid') {
			return;
		}

		try {
			const body: EditServiceDefinitionAttributeParams = {
				attribute_code: editAttribute.code,
				service_code: serviceCode,
				description: editAttribute.description.value,
				datatype_description: editAttribute.dataTypeDescription.value,
				required: editAttribute.required
			};

			if (editAttribute.values) {
				for (let value of editAttribute.values) {
					if (value.name == '') {
						multivalueErrorMessage = 'You might want to add a value!';
						return;
					}
				}
				body.values = editAttribute.values;
			}

			await libre311.editAttribute(body);

			updateAttributeMap(serviceCode);
			goto(`/groups/${groupId}/services/${serviceCode}`);
		} catch (error) {
			alertError(error);
		}
	}

	function addEditValue() {
		if (editAttribute.values == undefined) {
			return;
		}
		const newId = editAttribute.values?.length ? Number(editAttribute.values[editAttribute.values.length - 1].key) + 1 : 1;
		editAttribute.values = [...editAttribute.values, { key: newId.toString(), name: '' }];
	}

	function removeEditValue(index: number) {
		if (editAttribute.values) {
			for (let i = 0; i < editAttribute.values.length; i++) {
				if (i == index) {
					editAttribute.values = editAttribute.values.filter((_, i) => i !== index);
				}
			}
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
	</Card.Header>

	<Card.Content slot="content" class="p-0 sm:p-0">
		{#if asyncAttributeInputMap?.type === 'success'}
			<div
				class="mx-4"
				transition:fade={{ delay: 0, duration: 150 }}
			>
				<div class="my-2 flex items-center justify-between">
					<div class="my-2 items-center">
						<label for="is-edit-attribute-required">
							<strong class="text-base">
								{messages['serviceDefinitionEditor']['attributes']['required']}
							</strong>
						</label>
						<input
							class="mx-2 rounded-sm"
							id="is-edit-attribute-required"
							type="checkbox"
							bind:checked={editAttribute.required}
						/>
					</div>
				</div>

				<div class="my-2">
					<Input
						name="edit-attribute-description"
						error={editAttribute.description.error}
						bind:value={editAttribute.description.value}
						placeholder={messages['serviceDefinitionEditor']['attributes'][
							'description_placeholder'
						]}
					>
						<Input.Label slot="label">
							<strong class="text-base">
								{messages['serviceDefinitionEditor']['attributes']['description']}
							</strong>
						</Input.Label>
					</Input>
				</div>

				<div class="my-2">
					<Input
						name="edit-attribute-datatype-description"
						error={editAttribute.dataTypeDescription.error}
						bind:value={editAttribute.dataTypeDescription.value}
						placeholder={messages['serviceDefinitionEditor']['attributes'][
							'data_type_description_placeholder'
						]}
					>
						<Input.Label slot="label">
							<strong class="text-base">
								{messages['serviceDefinitionEditor']['attributes'][
									'data_type_description'
								]}
							</strong>
						</Input.Label>
					</Input>
				</div>

				{#if editAttribute.values}
					<div class="flex flex-col" transition:slide|local={{ duration: 500 }}>
						<strong class="text-base">{'Values'}</strong>

						<ul>
							{#each editAttribute.values as _, index}
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
										bind:value={editAttribute.values[index].name}
									/>

									{#if index != 0}
										<Button on:click={() => removeEditValue(index)}>
											<XMark />
										</Button>
									{/if}
								</li>
							{/each}
						</ul>

						<Button class="mt-1" type="ghost" on:click={addEditValue}>
							{'+ Add'}
						</Button>
					</div>
				{/if}

				<div class="my-2 flex items-center justify-between">
					<Button
						class="mr-1 w-1/2"
						aria-label="Close"
						type="ghost"
						on:click={() => window.history.back()}
					>
						{'Cancel'}
					</Button>

					<Button
						class="ml-1 w-1/2"
						aria-label="Submit"
						type="primary"
						on:click={handleEditAttribute}
					>
						{'Save Changes'}
					</Button>
				</div>
			</div>

		{:else if asyncAttributeInputMap?.type === 'inProgress'}
			<div class="mx-8 my-4">
				<Progress value={0} indeterminate />
			</div>
		{/if}
	</Card.Content>
</Card>
