<script lang="ts">
	import { page } from '$app/state';
	import { useLibre311Context } from '$lib/context/Libre311Context';
    import {Button} from "$lib/components/ui/button";

	const linkResolver = useLibre311Context().linkResolver;
</script>

<Button variant="link" href={linkResolver.issuesMap(page.url)}>Cancel</Button>
