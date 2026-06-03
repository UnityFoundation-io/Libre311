<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Button, Drawer, Menu, Modal, Portal } from 'stwui';
	import { fade } from 'svelte/transition';

	import { mapIcon } from './Svg/outline/mapIcon';
	import { pencilIcon } from './Svg/outline/pencilIcon';
	import { documentTextIcon } from './Svg/outline/documentTextIcon';
	import { tableIcon } from './Svg/outline/tableIcon';
	import { plusCircleIcon } from './Svg/outline/plusCircleIcon';
	import { user } from './Svg/outline/user';
	import { xMark } from './Svg/outline/XMark.svelte';
	import AuthGuard from './AuthGuard.svelte';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import { useSelectedProjectSlugStore } from '$lib/context/ServiceRequestsContext';

	export let open: boolean;

	export let handleClose: () => void;

	const linkResolver = useLibre311Context().linkResolver;
	const jurisdiction = useJurisdiction();
	const selectedProjectSlug = useSelectedProjectSlugStore();

	let showCreateModal = false;

	$: showRegular = !($jurisdiction.project_feature === 'REQUIRED' && !$selectedProjectSlug);
	$: showPhotoVoice = !!$jurisdiction.photo_voice_service_code && !$selectedProjectSlug;
	$: bothAvailable = showRegular && showPhotoVoice;

	function openCreateModal() {
		handleClose();
		showCreateModal = true;
	}
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
					{#if bothAvailable}
						<Menu.Item key="create" label="New Submission" href="javascript:void(0)" on:click={openCreateModal}>
							<Menu.Item.Icon slot="icon" data={plusCircleIcon} fill="none" />
						</Menu.Item>
					{:else if showRegular}
						<Menu.Item
							key="create"
							label="Report an Issue"
							href={linkResolver.issueCreate($page.url)}
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={plusCircleIcon} fill="none" />
						</Menu.Item>
					{:else if showPhotoVoice}
						<Menu.Item
							key="create"
							label="Submit Story"
							href="/photo-voice/create"
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={plusCircleIcon} fill="none" />
						</Menu.Item>
					{/if}
					<AuthGuard
						requires={[
							'LIBRE311_REQUEST_VIEW-TENANT',
							'LIBRE311_REQUEST_VIEW-SUBTENANT',
							'LIBRE311_REQUEST_VIEW-SYSTEM'
						]}
					>
						<Menu.Item
							key="table"
							label="Submissions Table"
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
					{#if $selectedProjectSlug && $jurisdiction.show_exit_project_mode !== false}
						<Menu.Item
							key="exit-project"
							label="Exit Project Mode"
							href="/issues/map"
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={xMark} fill="none" />
						</Menu.Item>
					{/if}
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
					</AuthGuard>
					{#if $jurisdiction.project_feature && $jurisdiction.project_feature !== 'DISABLED'}
						<AuthGuard
							requires={[
								'LIBRE311_ADMIN_EDIT-SYSTEM',
								'LIBRE311_ADMIN_EDIT-TENANT',
								'LIBRE311_ADMIN_EDIT-SUBTENANT',
								'LIBRE311_REQUEST_VIEW-SYSTEM',
								'LIBRE311_REQUEST_VIEW-TENANT',
								'LIBRE311_REQUEST_VIEW-SUBTENANT'
							]}
						>
							<Menu.Item
								key="projects"
								label="Project Management"
								href="/projects"
								on:click={handleClose}
							>
								<Menu.Item.Icon slot="icon" data={pencilIcon} fill="none" />
							</Menu.Item>
						</AuthGuard>
					{/if}
					<AuthGuard
						requires={[
							'LIBRE311_ADMIN_EDIT-SYSTEM',
							'LIBRE311_ADMIN_EDIT-TENANT',
							'LIBRE311_ADMIN_EDIT-SUBTENANT'
						]}
					>
						<Menu.Item
							key="policy-editor"
							label="Privacy Policy/T&C Editor"
							href="/admin/policy-editor"
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={documentTextIcon} fill="none" />
						</Menu.Item>
					</AuthGuard>
					<AuthGuard
						requires={[
							'LIBRE311_ADMIN_EDIT-SYSTEM',
							'LIBRE311_ADMIN_EDIT-TENANT',
							'LIBRE311_ADMIN_EDIT-SUBTENANT'
						]}
					>
						<Menu.Item
							key="system-admin"
							label="System Administration"
							href="/admin/system"
							on:click={handleClose}
						>
							<Menu.Item.Icon slot="icon" data={documentTextIcon} fill="none" />
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

	{#if showCreateModal}
		<Modal handleClose={() => (showCreateModal = false)}>
			<Modal.Content slot="content">
				<Modal.Content.Header slot="header">What would you like to submit?</Modal.Content.Header>
				<Modal.Content.Body slot="body">
					<div class="flex flex-col gap-3 p-4">
						<Button
							type="primary"
							href={linkResolver.issueCreate($page.url)}
							on:click={() => (showCreateModal = false)}
						>
							Report an Issue
						</Button>
						<Button href="/photo-voice/create" on:click={() => (showCreateModal = false)}>
							Submit Story
						</Button>
					</div>
				</Modal.Content.Body>
			</Modal.Content>
		</Modal>
	{/if}
</Portal>
