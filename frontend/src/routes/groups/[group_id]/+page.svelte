<script lang="ts">
	import { goto } from '$app/navigation';
	import { Breadcrumbs, Card, List } from 'stwui';

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
