<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Button } from 'stwui';

	const linkResolver = useLibre311Context().linkResolver;
</script>

<Button type="link" href={linkResolver.issuesMap($page.url)}>Cancel</Button>
