<script lang="ts">
	import { Button } from 'stwui';
	import PlusCircleIcon from './Svg/outline/PlusCircleIcon.svelte';
	import type { Maybe } from '$lib/utils/types';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import messages from '$media/messages.json';

	export let projectSlug: Maybe<string> = undefined;

	const jurisdictionStore = useJurisdiction();

	$: href = projectSlug ? `/issue/create?project_slug=${projectSlug}` : '/issue/create';
	$: showNewRequest = !($jurisdictionStore.project_feature === 'REQUIRED' && !projectSlug);
	$: showLogPoint = !!$jurisdictionStore.photo_voice_service_code && !projectSlug;
</script>

{#if showNewRequest || showLogPoint}
	<div class="absolute bottom-5 left-1/2 z-[1] flex -translate-x-1/2 gap-2">
		{#if showNewRequest}
			<Button type="primary" {href} class="whitespace-nowrap">
				<div slot="leading">
					<PlusCircleIcon />
				</div>
				{messages['map']['button_new_submission']}
			</Button>
		{/if}
		{#if showLogPoint}
			<Button type="primary" href="/photo-voice/create" class="whitespace-nowrap">
				{messages['map']['button_submit_story']}
			</Button>
		{/if}
	</div>
{/if}
