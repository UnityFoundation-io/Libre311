<script lang="ts">
	import AdminTable from '$lib/components/AdminTable.svelte';
	import { Button, Drawer, Portal } from 'stwui';

	let drawerLeftOpen = false;

	function openDrawerLeft() {
		drawerLeftOpen = true;
	}

	function closeDrawerLeft() {
		drawerLeftOpen = false;
	}
</script>

<Button type="primary" on:click={openDrawerLeft}>Open Left</Button>

<Portal>
	{#if drawerLeftOpen}
		<Drawer handleClose={closeDrawerLeft} placement="left">
			<Drawer.Content slot="content">Drawer Content</Drawer.Content>
		</Drawer>
	{/if}
</Portal>

<AdminTable />
