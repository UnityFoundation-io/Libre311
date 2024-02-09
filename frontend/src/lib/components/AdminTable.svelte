<script lang="ts">
	import { Badge, Card, Input, Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';

	const columns: TableColumn[] = [
		{
			column: 'project_name',
			label: 'Project Name',
			placement: 'left',
			class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'left',
			class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'city',
			label: 'City',
			placement: 'left',
			class: 'hidden md:table-cell w-0 md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'state',
			label: 'State',
			placement: 'left',
			class: 'hidden lg:table-cell lg:w-[20%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'right',
			class: 'w-[20%] md:w-[10%] lg:w-[20%]'
		}
	];

	interface Item extends Record<string, string> {
		id: string;
		project_name: string;
		status: string;
		city: string;
		state: string;
		created_at: string;
	}

	let data: { results: Item[] } = {
		results: [
			{
				id: 'a',
				project_name: 'Libre311',
				status: 'Working on it',
				city: 'St. Louis',
				state: 'Misery',
				created_at: 'Unity Foundation'
			}
		]
	};
</script>

<Card bordered={false} class="h-[calc(100vh-14rem)]">
	<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
		Card Header
		<Input slot="extra" />
	</Card.Header>
	<Card.Content slot="content" class="p-0 sm:p-0" style="height: calc(100% - 64px);">
		<Table class="h-full overflow-hidden rounded-md" {columns}>
			<Table.Body slot="body">
				{#each data.results as item}
					<Table.Body.Row id="item.id">
						<Table.Body.Row.Cell column={0}>{item.project_name}</Table.Body.Row.Cell>
						<Table.Body.Row.Cell column={1}>{item.status}</Table.Body.Row.Cell>
						<Table.Body.Row.Cell column={2}>{item.city}</Table.Body.Row.Cell>
						<Table.Body.Row.Cell column={3}>{item.state}</Table.Body.Row.Cell>
						<Table.Body.Row.Cell column={4}>{item.created_at}</Table.Body.Row.Cell>
					</Table.Body.Row>
				{/each}
			</Table.Body>
		</Table>
	</Card.Content>
</Card>
