<script lang="ts">
	import { goto } from '$app/navigation';
	import { Breadcrumbs, Card, List } from 'stwui';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: '/groups/1/' },
		{ label: 'Attributes', href: '/groups/1/services/1' }
	];

	const attributes = [
		{
			id: 1,
			title: 'Something Old'
		},
		{
			id: 2,
			title: 'Something New'
		},
		{
			id: 3,
			title: 'Something Borrowed'
		},
		{
			id: 4,
			title: 'Something Blue'
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
			{#each attributes as attribute}
				<List.Item
					on:click={() => goto(`/groups/1/services/${attribute.id}`)}
					class="cursor-pointer hover:bg-slate-100"
				>
					<div class="mx-4">{attribute.title}</div></List.Item
				>
			{/each}
		</List>
	</Card.Content>
</Card>

<h1>Services</h1>

<div>create attribute</div>

<div>list attributes</div>

<div>edit attribute</div>

<div>delete attribute</div>
