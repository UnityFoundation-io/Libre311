<script lang="ts">
	import { goto } from '$app/navigation';
	import { ellipsisSVG } from '$lib/components/Svg/outline/EllipsisVertical.svelte';
	import { Breadcrumbs, Button, Card, List } from 'stwui';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [{ label: 'Groups', href: '/groups' }];

	const groups = [
		{
			id: 1,
			title: 'Parks Department'
		},
		{
			id: 2,
			title: 'Traffic'
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
			{#each groups as group}
				<List.Item
					on:click={() => goto(`/groups/${group.id}`)}
					class="cursor-pointer hover:bg-slate-100"
				>
					<List.Item.Content slot="content">
						<List.Item.Content.Title slot="title" class="mx-4">
							{group.title}
						</List.Item.Content.Title>
					</List.Item.Content>
					<List.Item.Extra slot="extra" placement="start">
						<Button type="ghost" shape="circle">
							<Button.Icon data={ellipsisSVG} />
						</Button>
					</List.Item.Extra>
				</List.Item>
			{/each}
		</List>
	</Card.Content>
</Card>
