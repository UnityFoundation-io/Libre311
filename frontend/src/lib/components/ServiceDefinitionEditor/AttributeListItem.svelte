<script lang="ts">
	import messages from '$media/messages.json';
	import { page } from '$app/stores';
	import {
		createAttributeInputMap,
		type AttributeInputMap
	} from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncFailure,
		type AsyncResult,
		asAsyncSuccess
	} from '$lib/services/http';
	import { createInput, stringValidator } from '$lib/utils/validation';
	import { Breadcrumbs, Button, Card, Input, Progress } from 'stwui';
	import { fade } from 'svelte/transition';
	import type { EditServiceDefinitionAttributeParams } from '$lib/services/Libre311/Libre311';
	import { goto } from '$app/navigation';
	import type { AttributeEditInput } from './types';
	import MultiValueList from './AttributeValue/MultiValueList.svelte';

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = ASYNC_IN_PROGRESS;
	let groupId = $page.params.group_id;
	let serviceCode = Number($page.params.service_id);
	let attributeCode = Number($page.params.attribute_id);
	let groupName = '';
	let serviceName = '';
	let editAttributeInput: AttributeEditInput = {
		code: 0,
		required: false,
		description: createInput<string>(),
		dataTypeDescription: createInput<string>(),
		values: undefined
	};
	let multivalueErrorMessage: string | undefined;

	$: multivalueErrorIndex = -1;

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
					editAttributeInput.code = attribute.code;
					editAttributeInput.required = attribute.required;
					editAttributeInput.description.value = attribute.description;
					editAttributeInput.dataTypeDescription.value = attribute.datatype_description?.toString();
					console.log(typeof attribute);

					if (
						attribute.datatype == 'multivaluelist' ||
						(attribute.datatype == 'singlevaluelist' && attribute.values)
					)
						editAttributeInput.values = attribute.values.sort(
							(a, b) => Number(a.key) - Number(b.key)
						);
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
		editAttributeInput.description = stringValidator(editAttributeInput.description);
		editAttributeInput.dataTypeDescription = stringValidator(
			editAttributeInput.dataTypeDescription
		);

		// Return if any input errors
		if (editAttributeInput.description.type != 'valid') {
			return;
		}
		if (editAttributeInput.dataTypeDescription.type != 'valid') {
			return;
		}

		try {
			const body: EditServiceDefinitionAttributeParams = {
				attribute_code: editAttributeInput.code,
				service_code: serviceCode,
				description: editAttributeInput.description.value,
				datatype_description: editAttributeInput.dataTypeDescription.value,
				required: editAttributeInput.required
			};

			if (editAttributeInput.values) {
				for (let i = 0; i < editAttributeInput.values.length; i++) {
					if (editAttributeInput.values[i].name == '') {
						multivalueErrorMessage = `You might want to add a value!`;
						multivalueErrorIndex = i;
						return;
					} else {
						multivalueErrorMessage = undefined;
					}
				}
				body.values = editAttributeInput.values;
			}

			await libre311.editAttribute(body);

			updateAttributeMap(serviceCode);
			goto(`/groups/${groupId}/services/${serviceCode}`);
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
	</Card.Header>

	<Card.Content slot="content" class="p-0 sm:p-0">
		{#if asyncAttributeInputMap?.type === 'success'}
			<div class="mx-4" transition:fade={{ delay: 0, duration: 150 }}>
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
							bind:checked={editAttributeInput.required}
						/>
					</div>
				</div>

				<div class="my-2">
					<Input
						name="edit-attribute-description"
						error={editAttributeInput.description.error}
						bind:value={editAttributeInput.description.value}
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
						error={editAttributeInput.dataTypeDescription.error}
						bind:value={editAttributeInput.dataTypeDescription.value}
						placeholder={messages['serviceDefinitionEditor']['attributes'][
							'data_type_description_placeholder'
						]}
					>
						<Input.Label slot="label">
							<strong class="text-base">
								{messages['serviceDefinitionEditor']['attributes']['data_type_description']}
							</strong>
						</Input.Label>
					</Input>
				</div>

				{#if editAttributeInput.values}
					<MultiValueList
						bind:values={editAttributeInput.values}
						{multivalueErrorMessage}
						{multivalueErrorIndex}
					/>
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
