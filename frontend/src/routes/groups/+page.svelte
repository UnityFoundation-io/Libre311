<script lang="ts" context="module">
	let cachedServiceList: GetServiceListResponse | undefined = undefined;
</script>

<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { GetServiceListResponse, ServiceCode } from '$lib/services/Libre311/Libre311';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { Breadcrumbs, Card, List, Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import { onMount } from 'svelte';

	interface Crumb {
		label: string;
		href: string;
	}

	const crumbs: Crumb[] = [{ label: 'Groups', href: '/groups' }];
	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;

	function fetchServiceList() {
		if (cachedServiceList) {
			serviceList = asAsyncSuccess(cachedServiceList);
			return;
		}
		libre311
			.getServiceList()
			.then((res) => {
				cachedServiceList = res;
				serviceList = asAsyncSuccess(res);
			})
			.catch((err) => (serviceList = asAsyncFailure(err)));
	}

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	onMount(fetchServiceList);
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

	{#if serviceList.type === 'success'}
		{@const selectOptions = createSelectOptions(serviceList.value)}
		<Card.Content slot="content" class="p-0 sm:p-0">
			<List>
				{#each selectOptions as group}
					<List.Item
						on:click={() => goto(`/groups/${group.value}`)}
						class="cursor-pointer hover:bg-slate-100"
					>
						<div class="mx-4">{group.label}</div>
					</List.Item>
				{/each}
			</List>
		</Card.Content>
	{/if}
</Card>
