<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { CreateServiceResponse } from '$lib/services/Libre311/types/CreateService';
	import {
		stringValidator,
		type FormInputValue,
		numberValidator,
		createInput
	} from '$lib/utils/validation';
	import {
		Breadcrumbs,
		Button,
		Card,
		Input,
		InputNumber,
		List,
		Modal,
		Portal,
		TextArea
	} from 'stwui';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: '/groups/1/' }
	];

	const services = [
		{
			id: 1,
			title: 'Animal Attack'
		},
		{
			id: 2,
			title: 'Playground Equipment'
		},
		{
			id: 3,
			title: 'Trail Maintenance'
		}
	];

	const libre311 = useLibre311Service();

	async function handleAddNewService() {
		newServiceName = stringValidator(newServiceName);
		newServiceCode = stringValidator(newServiceCode);
		newServiceDescription = stringValidator(newServiceDescription);
		newServiceDefinition = stringValidator(newServiceDefinition);
		newServiceGroupId = numberValidator(newServiceGroupId);

		const resultSet = new Set([
			newServiceName.type,
			newServiceCode.type,
			newServiceDescription.type,
			newServiceDefinition.type,
			newServiceGroupId.type
		]);
		if (resultSet.has('invalid')) return;

		await libre311.createService({
			service_code: newServiceCode.value ?? '',
			service_name: newServiceName.value ?? '',
			description: newServiceDescription.value ?? '',
			service_definition: newServiceDefinition.value ?? '',
			group_id: newServiceGroupId.value ?? -1
		});

		isAddServiceModalOpen = false;
	}

	let isAddServiceModalOpen: boolean = false;

	let newServiceName: FormInputValue<string> = createInput();
	let newServiceCode: FormInputValue<string> = createInput();
	let newServiceDescription: FormInputValue<string> = createInput();
	let newServiceDefinition: FormInputValue<string> = createInput();
	let newServiceGroupId: FormInputValue<number> = createInput();
</script>

<Portal>
	{#if isAddServiceModalOpen}
		<Modal
			handleClose={() => {
				isAddServiceModalOpen = false;
			}}
		>
			<Modal.Content slot="content" class="max-h-full">
				<Modal.Content.Header slot="header" class="h-16">
					<h1 class="text-lg">Add A New Service</h1>
				</Modal.Content.Header>
				<Modal.Content.Body slot="body" class="overflow-y-auto">
					<Input
						class="m-2"
						name="new-service-code"
						error={newServiceCode.error}
						bind:value={newServiceCode.value}
					>
						<Input.Label slot="label">Code:</Input.Label>
					</Input>

					<Input
						class="m-2"
						name="new-service-name"
						error={newServiceName.error}
						bind:value={newServiceName.value}
					>
						<Input.Label slot="label">Name:</Input.Label>
					</Input>

					<TextArea
						class="m-2"
						name="new-service-description"
						error={newServiceDescription.error}
						bind:value={newServiceDescription.value}
					>
						<TextArea.Label slot="label">Description:</TextArea.Label>
					</TextArea>

					<TextArea
						class="m-2"
						name="new-service-definition"
						error={newServiceDefinition.error}
						bind:value={newServiceDefinition.value}
					>
						<TextArea.Label slot="label">Definition:</TextArea.Label>
					</TextArea>

					<InputNumber
						class="m-2"
						name="new-service-name"
						error={newServiceGroupId.error}
						bind:value={newServiceGroupId.value}
					>
						<InputNumber.Label slot="label">Group ID:</InputNumber.Label>
					</InputNumber>
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex items-center justify-center">
						<Button
							class="m-1 w-1/2"
							on:click={() => {
								isAddServiceModalOpen = false;
							}}>Cancel</Button
						>
						<Button class="m-1 w-1/2" type="primary" on:click={handleAddNewService}>Add</Button>
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	{/if}
</Portal>

<div class="m-4 flex justify-end">
	<Button
		type="ghost"
		on:click={() => {
			isAddServiceModalOpen = true;
		}}>{'+ Add Service'}</Button
	>
</div>

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
		<List>
			{#each services as service}
				<List.Item
					on:click={() => goto(`/groups/1/services/${service.id}`)}
					class="cursor-pointer hover:bg-slate-100"
				>
					<div class="mx-4">{service.title}</div></List.Item
				>
			{/each}
		</List>
	</Card.Content>
</Card>
