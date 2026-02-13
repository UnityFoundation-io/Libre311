<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Drawer, Menu, Portal } from 'stwui';
	import { fade } from 'svelte/transition';

	import { mapIcon } from './Svg/outline/mapIcon';
	import { pencilIcon } from './Svg/outline/pencilIcon';
	import { tableIcon } from './Svg/outline/tableIcon';
	import { plusCircleIcon } from './Svg/outline/plusCircleIcon';
	import { user } from './Svg/outline/user';
	import AuthGuard from './AuthGuard.svelte';

	export let open: boolean;

	export let handleClose: () => void;

	const linkResolver = useLibre311Context().linkResolver;
</script>

<Portal>
	{#if open}
		<Drawer {handleClose} placement="left">
			<div
				slot="backdrop"
				class="fixed inset-0 bg-black/10 backdrop-blur-sm transition-opacity"
				aria-hidden="true"
				on:click={handleClose}
				transition:fade
			/>
			<Drawer.Content slot="content">
				<Menu>
					<Menu.Item
						key="create"
						label="Create Service Request"
						href="/issue/create"
						on:click={handleClose}
					>
						<Menu.Item.Icon slot="icon" data={plusCircleIcon} fill="none" />
					</Menu.Item>
					<AuthGuard
						requires={[
							'LIBRE311_REQUEST_VIEW-TENANT',
							'LIBRE311_REQUEST_VIEW-SUBTENANT',
							'LIBRE311_REQUEST_VIEW-SYSTEM'
						]}
					>
						<Menu.Item
							key="table"
							label="Service Request Table"
							href={linkResolver.issuesTable($page.url)}
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={tableIcon} fill="none" />
						</Menu.Item>
					</AuthGuard>
					<Menu.Item
						key="map"
						label="Request Map"
						href={linkResolver.issuesMap($page.url)}
						on:click={handleClose}
					>
						<Menu.Item.Icon slot="icon" data={mapIcon} fill="none" />
					</Menu.Item>
					<AuthGuard
						requires={[
							'LIBRE311_ADMIN_EDIT-SYSTEM',
							'LIBRE311_ADMIN_EDIT-TENANT',
							'LIBRE311_ADMIN_EDIT-SUBTENANT'
						]}
					>
						<Menu.Item
							key="manager"
							label="Service Definition Configuration"
							href="/groups/config"
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={pencilIcon} fill="none" />
						</Menu.Item>
						<Menu.Item
							key="projects"
							label="Project Management"
							href="/projects"
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={pencilIcon} fill="none" />
						</Menu.Item>
					</AuthGuard>
					<AuthGuard requires="is-anonymous">
						<Menu.Item key="login" label="Login" href="/login" on:click={handleClose}>
							<Menu.Item.Icon slot="icon" data={user} fill="none" />
						</Menu.Item>
					</AuthGuard>
				</Menu>
			</Drawer.Content>
		</Drawer>
	{/if}
</Portal>
