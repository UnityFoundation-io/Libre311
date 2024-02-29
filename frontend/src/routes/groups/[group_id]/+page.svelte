<script lang="ts">
	import { goto } from '$app/navigation';
	import { Breadcrumbs, Button, Card, List, Modal, Portal } from 'stwui';

	import { ellipsisSVG } from '$lib/components/Svg/outline/EllipsisVertical.svelte';

	let modalOpen: boolean = false;

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

	function openModal() {
		modalOpen = true;
	}

	function closeModal() {
		modalOpen = false;
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
		<List>
			{#each services as service}
				<List.Item>
					<List.Item.Content slot="content">
						<List.Item.Content.Title slot="title">
							{service.title}
						</List.Item.Content.Title>
					</List.Item.Content>
					<List.Item.Extra slot="extra" placement="start">
						<Button type="ghost" shape="circle" on:click={openModal}>
							<Button.Icon data={ellipsisSVG} />
						</Button>
					</List.Item.Extra>
				</List.Item>
			{/each}
		</List>
	</Card.Content>
</Card>

<Portal>
	{#if modalOpen}
		<Modal handleClose={closeModal}>
			<Modal.Content slot="content">
				<Modal.Content.Header slot="header">Modal</Modal.Content.Header>
				<Modal.Content.Body slot="body">I am the content</Modal.Content.Body>
			</Modal.Content>
		</Modal>
	{/if}
</Portal>
