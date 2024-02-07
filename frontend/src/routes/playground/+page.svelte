<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Button } from 'stwui';

	import MenuDrawer from '$lib/components/MenuDrawer.svelte';

	let isOpen: boolean;

	function openDrawer() {
		isOpen = !isOpen;
	}
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar">
		<Button type="primary" on:click={openDrawer}>Open Menu</Button>

		<MenuDrawer bind:open={isOpen} />
	</slot>
	<div slot="main-content" class="relative flex h-full" />
</SideBarMainContentLayout>
