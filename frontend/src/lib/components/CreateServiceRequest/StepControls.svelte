<!-- @migration-task Error while migrating Svelte code: This migration would change the name of a slot (submit-text to submit_text) making the component unusable -->
<script lang="ts">
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import messages from '$media/messages.json';
	import { page } from '$app/stores';
    import {Button} from "$lib/components/ui/button";

	export let loading: boolean | undefined = undefined;

	const linkResolver = useLibre311Context().linkResolver;
</script>

<div class="mb-4 flex items-center justify-between">
	<Button href={linkResolver.createIssuePagePrevious($page.url)}>
		{messages['contact']['button']['back']}
	</Button>

	<Button {loading} on:click>
		<slot name="submit-text" />
	</Button>
</div>
