<script lang="ts">
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Button } from 'stwui';
	import messages from '$media/messages.json';
	import { page } from '$app/stores';

	export let loading: boolean | undefined = undefined;

	const linkResolver = useLibre311Context().linkResolver;
</script>

<div class="mb-4 flex items-center justify-between">
	<Button href={linkResolver.createIssuePagePrevious($page.url)}>
		{messages['contact']['button']['back']}
	</Button>

	<Button {loading} type="primary" on:click>
		<slot name="submit-text" />
	</Button>
</div>
