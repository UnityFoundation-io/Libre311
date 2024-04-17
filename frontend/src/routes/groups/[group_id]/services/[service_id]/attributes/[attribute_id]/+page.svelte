<script lang="ts">
	import { page } from "$app/stores";
	import { createAttributeInputMap, type AttributeInputMap } from "$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared";
	import { useLibre311Context, useLibre311Service } from "$lib/context/Libre311Context";
	import { ASYNC_IN_PROGRESS, asAsyncFailure, type AsyncResult, asAsyncSuccess } from "$lib/services/http";
	import { createInput, type FormInputValue } from "$lib/utils/validation";
	import { Breadcrumbs, Card, Progress } from "stwui";

	type AttributeEdit = {
		code: number;
		required: boolean;
		description: FormInputValue<string>;
		dataTypeDescription: FormInputValue<string>;
	};

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> = ASYNC_IN_PROGRESS;
	let groupId = $page.params.group_id;
	let serviceCode = Number($page.params.service_id);
	let groupName = '';
	let serviceName = '';

	let editAttribute: AttributeEdit = {
		code: 0,
		required: false,
		description: createInput<string>(),
		dataTypeDescription: createInput<string>()
	};

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

			// Get Service ID
			const serviceList = await libre311.getServiceList();
			for (let service of serviceList) {
				if (service.service_code == serviceCode) {
					serviceName = service.service_name;
				}
			}

			asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(res, {}));
		} catch (error) {
			asyncAttributeInputMap = asAsyncFailure(error);
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
			TODO
		{:else if asyncAttributeInputMap?.type === 'inProgress'}
			<div class="mx-8 my-4">
				<Progress value={0} indeterminate />
			</div>
		{/if}
	</Card.Content>
</Card>
