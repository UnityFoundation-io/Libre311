<script lang="ts">
	import { Breadcrumbs, Button, Card, Dropdown, List } from 'stwui';
	import { asAsyncSuccess, type AsyncResult } from '$lib/services/http';
	import { createAttributeInputMap, type AttributeInputMap } from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';

	const libre311 = useLibre311Service();

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> | undefined;
	let selectedServiceCode: string = $page.url.pathname.replace(/\/groups\/(\d+)\/services\//, '');

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [
		{ label: 'Groups', href: '/groups' },
		{ label: 'Services', href: '/groups/1/' },
		{ label: 'Attributes', href: '/groups/1/services/1' }
	];

	$: updateAttributeMap(selectedServiceCode);

	function updateAttributeMap(service: string) {
		if (!service) {
			asyncAttributeInputMap = undefined;
			return;
		}
		getServiceDefinition(service);
	}

	async function getServiceDefinition(selectedServiceCode: string) {
		asyncAttributeInputMap = undefined;

		const payload = { service_code: selectedServiceCode };
		const res = await libre311.getServiceDefinition(payload);
		asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(res, {}));
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

	{#if asyncAttributeInputMap?.type == 'success'}
		<Card.Content slot="content" class="p-0 sm:p-0">
			<List>
				{#each asyncAttributeInputMap.value.values() as input}
					<List.Item class="flex items-center cursor-pointer hover:bg-slate-100">

						<div class="mx-4 w-full">
							{input.attribute.code}
						</div>

					</List.Item>
				{/each}
			</List>
		</Card.Content>
	{/if}
</Card>
