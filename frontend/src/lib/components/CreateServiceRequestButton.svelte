<script lang="ts">
	import { Button } from 'stwui';
	import PlusCircleIcon from './Svg/outline/PlusCircleIcon.svelte';
	import type { Maybe } from '$lib/utils/types';

	export let projectSlug: Maybe<string> = undefined;

	$: href = projectSlug ? `/issue/create?project_slug=${projectSlug}` : '/issue/create';
</script>

<div class="absolute bottom-5 left-1/2 z-[1] -translate-x-1/2">
	<Button type="primary" {href} class="whitespace-nowrap">
		<div slot="leading">
			<PlusCircleIcon />
		</div>
		New Request
	</Button>
</div>
