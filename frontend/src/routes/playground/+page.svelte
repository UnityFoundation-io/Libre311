<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Button } from 'stwui';

	import DrawerMenu from '$lib/components/DrawerMenu.svelte';

	let isOpen: boolean = false;

	function openDrawer() {
		isOpen = !isOpen;
	}
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar">
		<Button type="primary" on:click={openDrawer}>Open Menu</Button>

		<DrawerMenu open={isOpen} />
	</slot>
	<div slot="main-content" class="relative flex h-full">map</div>
</SideBarMainContentLayout>
