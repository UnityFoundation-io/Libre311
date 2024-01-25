<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		GetServiceListResponse,
		Service,
		ServiceCode,
		ServiceDefinition
	} from '$lib/services/Libre311/Libre311';

	import { ASYNC_IN_PROGRESS, asAsyncSuccess, type AsyncResult } from '$lib/services/http';
	import messages from '$media/messages.json';

	import { Button, Select, TextArea } from 'stwui';

	// Service Definition Attributes
	import StringServiceDefinitionAttribute from '$lib/components/ServiceDefinitionAttributes/StringServiceDefinitionAttribute.svelte';
	import MultiSelectServiceDefinitionAttribute from '$lib/components/ServiceDefinitionAttributes/MultiSelectServiceDefinitionAttribute.svelte';

	import type { SelectOption } from 'stwui/types';
	import { onMount } from 'svelte';

	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let selectedServiceCode: ServiceCode | undefined;
	let serviceDefinition: AsyncResult<ServiceDefinition> | undefined;

	$: if (selectedServiceCode) getServiceDefinition(selectedServiceCode);

	function issueTypeChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedServiceCode = target.value;
	}

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	async function getServiceDefinition(service_code: ServiceCode) {
		const res = await libre311.getServiceDefinition({ service_code });
		serviceDefinition = asAsyncSuccess(res);
	}

	onMount(async () => {
		const res = await libre311.getServiceList();
		serviceList = asAsyncSuccess(res);
	});

	$: console.log(serviceList);
	$: console.log(serviceDefinition);
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar">
		<div class="relative mx-8 my-4">{messages['reporting']['timestamp']}</div>
		<div class="relative mx-8 my-4">
			{messages['reporting']['location']}
		</div>
		{#if serviceList.type === 'success'}
			{@const selectOptions = createSelectOptions(serviceList.value)}
			<Select
				name="select-1"
				placeholder="Issue Type"
				on:change={issueTypeChange}
				options={selectOptions}
				class="relative mx-8 my-4"
			>
				<Select.Options slot="options">
					{#each selectOptions as option}
						<Select.Options.Option {option} />
					{/each}
				</Select.Options>
			</Select>
			{#if serviceDefinition?.type === 'success'}
				{#each serviceDefinition.value.attributes as attribute}
					{#if attribute.datatype === 'multivaluelist'}
						<MultiSelectServiceDefinitionAttribute {attribute} on:change={(e) => console.log(e)} />
					{/if}
					<!--           
          {#if attribute.datatype === 'string'}
            <StringServiceDefinitionAttribute {attribute} on:change={(e) => console.log(e)}/>
          {/if}
           -->
				{/each}
			{/if}
		{/if}

		<TextArea name="comments" placeholder="Citizen Comments" class="relative mx-8 my-4" />

		<div class="mx-8 my-4 flex justify-between">
			<Button href="/issues/map">Back</Button>
			<Button type="primary">Submit</Button>
		</div>
	</slot>
	<div slot="main-content">map goes here</div>
</SideBarMainContentLayout>
