<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar" />
	<div slot="main-content">map goes here</div>
</SideBarMainContentLayout>
