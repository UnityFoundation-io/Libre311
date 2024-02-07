<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Button } from 'stwui';

	import MenuDrawer from '$lib/components/MenuDrawer.svelte';

	let isOpen: boolean = false;

	function openDrawer() {
		isOpen = !isOpen;
	}

	function closeDrawer() {
		isOpen = false;
	}
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar">
		<Button type="primary" on:click={openDrawer}>Open Menu</Button>
	</slot>
	<div slot="main-content" class="relative flex h-full">
		<MenuDrawer open={isOpen} handleClose={closeDrawer} />
	</div>
</SideBarMainContentLayout>
