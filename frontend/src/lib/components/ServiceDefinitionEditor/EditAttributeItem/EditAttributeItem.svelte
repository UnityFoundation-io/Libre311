<script lang="ts">
	import { run } from 'svelte/legacy';

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
	import { createInput, stringValidator, type FormInputValue } from '$lib/utils/validation';
	import { Breadcrumbs, Card, Input, Progress } from 'stwui';
	import { Button } from '$lib/components/ui/button';
	import { fade } from 'svelte/transition';
	import { type AttributeValue } from '$lib/services/Libre311/Libre311';
	import { goto } from '$app/navigation';
	import EditMultiValueList from './EditMultiValueList.svelte';

	interface EditAttributeInput {
		attribute_code: number;
		required: boolean;
		description: FormInputValue<string>;
		dataTypeDescription: FormInputValue<string>;
		values: AttributeValue[] | undefined;
	}

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = $state(ASYNC_IN_PROGRESS);
	let groupId = $page.params.group_id;
	let serviceCode = Number($page.params.service_id);
	let attributeCode = Number($page.params.attribute_id);
	let groupName = $state('');
	let serviceName = $state('');
	let editAttributeInput: EditAttributeInput = $state({
		attribute_code: 0,
		required: false,
		description: createInput<string>(),
		dataTypeDescription: createInput<string>(),
		values: undefined
	});



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
					editAttributeInput.attribute_code = attribute.code;
					editAttributeInput.required = attribute.required;
					editAttributeInput.description.value = attribute.description;
					editAttributeInput.dataTypeDescription.value = attribute.datatype_description?.toString();

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

	async function handleEditStringAttribute() {
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

		const body = {
			attribute_code: editAttributeInput.attribute_code,
			service_code: serviceCode,
			description: editAttributeInput.description.value?.toString(),
			datatype_description: editAttributeInput.dataTypeDescription.value?.toString(),
			required: editAttributeInput.required
		};

		try {
			await libre311.editAttribute(body);

			updateAttributeMap(serviceCode);
			goto(`/groups/${groupId}/services/${serviceCode}`);
		} catch (error) {
			alertError(error);
		}
	}

	async function handleEditMultivaluelistAttribute(e: CustomEvent) {
		const values = e.detail.values;

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

		const body = {
			attribute_code: editAttributeInput.attribute_code,
			service_code: serviceCode,
			description: editAttributeInput.description.value?.toString(),
			datatype_description: editAttributeInput.dataTypeDescription.value?.toString(),
			required: editAttributeInput.required,
			values: values
		};

		try {
			await libre311.editAttribute(body);

			updateAttributeMap(serviceCode);
			goto(`/groups/${groupId}/services/${serviceCode}`);
		} catch (error) {
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
							{#snippet label()}
												<Input.Label >
									<strong class="text-base">
										{messages['serviceDefinitionEditor']['attributes']['description']}
									</strong>
								</Input.Label>
											{/snippet}
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
							{#snippet label()}
												<Input.Label >
									<strong class="text-base">
										{messages['serviceDefinitionEditor']['attributes']['data_type_description']}
									</strong>
								</Input.Label>
											{/snippet}
						</Input>
					</div>

					{#if editAttributeInput.values}
						<EditMultiValueList
							bind:attribute={editAttributeInput}
							on:submit={handleEditMultivaluelistAttribute}
						/>
					{:else}
						<div class="my-2 flex items-center justify-between">
							<Button
								class="mr-1 w-1/2"
								aria-label="Close"
								variant="ghost"
								on:click={() => window.history.back()}
							>
								{'Cancel'}
							</Button>

							<Button
								class="ml-1 w-1/2"
								aria-label="Submit"
								variant="default"
								on:click={handleEditStringAttribute}
							>
								{'Save Changes'}
							</Button>
						</div>
					{/if}
				</div>
			{:else if asyncAttributeInputMap?.type === 'inProgress'}
				<div class="mx-8 my-4">
					<Progress value={0} indeterminate />
				</div>
			{/if}
		</Card.Content>
	{/snippet}
</Card>
