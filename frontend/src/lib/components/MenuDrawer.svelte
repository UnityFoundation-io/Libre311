<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	// import { Drawer, Menu, Portal } from 'stwui';

	import { mapIcon } from './Svg/outline/mapIcon';
	import { pencilIcon } from './Svg/outline/pencilIcon';
	import { tableIcon } from './Svg/outline/tableIcon';
	import { plusCircleIcon } from './Svg/outline/plusCircleIcon';
	import { user } from './Svg/outline/user';
	import AuthGuard from './AuthGuard.svelte';


	interface Props {
		open: boolean;
		handleClose: () => void;
	}

	let { open, handleClose }: Props = $props();

	const linkResolver = useLibre311Context().linkResolver;
</script>

<!--<Portal>-->
<!--	{#if open}-->
<!--		<Drawer {handleClose} placement="left">-->
<!--			{#snippet content()}-->
<!--						<Drawer.Content >-->
<!--					<Menu>-->
<!--						<Menu.Item-->
<!--							key="create"-->
<!--							label="Create Service Request"-->
<!--							href="/issue/create"-->
<!--							on:click={handleClose}-->
<!--						>-->
<!--							{#snippet icon()}-->
<!--												<Menu.Item.Icon  data={plusCircleIcon} fill="none" />-->
<!--											{/snippet}-->
<!--						</Menu.Item>-->
<!--						<AuthGuard-->
<!--							requires={[-->
<!--								'LIBRE311_REQUEST_VIEW-TENANT',-->
<!--								'LIBRE311_REQUEST_VIEW-SUBTENANT',-->
<!--								'LIBRE311_REQUEST_VIEW-SYSTEM'-->
<!--							]}-->
<!--						>-->
<!--							<Menu.Item-->
<!--								key="table"-->
<!--								label="Service Request Table"-->
<!--								href={linkResolver.issuesTable($page.url)}-->
<!--								on:click={handleClose}-->
<!--							>-->
<!--								{#snippet icon()}-->
<!--														<Menu.Item.Icon  data={tableIcon} fill="none" />-->
<!--													{/snippet}-->
<!--							</Menu.Item>-->
<!--						</AuthGuard>-->
<!--						<Menu.Item-->
<!--							key="map"-->
<!--							label="Request Map"-->
<!--							href={linkResolver.issuesMap($page.url)}-->
<!--							on:click={handleClose}-->
<!--						>-->
<!--							{#snippet icon()}-->
<!--												<Menu.Item.Icon  data={mapIcon} fill="none" />-->
<!--											{/snippet}-->
<!--						</Menu.Item>-->
<!--						<AuthGuard-->
<!--							requires={[-->
<!--								'LIBRE311_ADMIN_EDIT-SYSTEM',-->
<!--								'LIBRE311_ADMIN_EDIT-TENANT',-->
<!--								'LIBRE311_ADMIN_EDIT-SUBTENANT'-->
<!--							]}-->
<!--						>-->
<!--							<Menu.Item-->
<!--								key="manager"-->
<!--								label="Service Definition Configuration"-->
<!--								href="/groups"-->
<!--								on:click={handleClose}-->
<!--							>-->
<!--								{#snippet icon()}-->
<!--														<Menu.Item.Icon  data={pencilIcon} fill="none" />-->
<!--													{/snippet}-->
<!--							</Menu.Item>-->
<!--						</AuthGuard>-->
<!--						<AuthGuard requires="is-anonymous">-->
<!--							<Menu.Item key="login" label="Login" href="/login" on:click={handleClose}>-->
<!--								{#snippet icon()}-->
<!--														<Menu.Item.Icon  data={user} fill="none" />-->
<!--													{/snippet}-->
<!--							</Menu.Item>-->
<!--						</AuthGuard>-->
<!--					</Menu>-->
<!--				</Drawer.Content>-->
<!--					{/snippet}-->
<!--		</Drawer>-->
<!--	{/if}-->
<!--</Portal>-->
