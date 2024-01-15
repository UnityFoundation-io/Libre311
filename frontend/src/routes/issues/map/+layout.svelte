<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';

	// create the issues context / or load issues and set to state value
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar" />
	<div slot="main-content">map goes here</div>
</SideBarMainContentLayout>
