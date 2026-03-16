<script lang="ts">
	import ProjectEditor from '$lib/components/ProjectEditor.svelte';
	import AuthGuard from '$lib/components/AuthGuard.svelte';
</script>

<svelte:head>
	<title>Create Project</title>
</svelte:head>

<AuthGuard
	requires={[
		'LIBRE311_ADMIN_EDIT-TENANT',
		'LIBRE311_ADMIN_EDIT-SYSTEM',
		'LIBRE311_ADMIN_EDIT-SUBTENANT'
	]}
>
	<div class="h-full">
		<ProjectEditor />
	</div>
</AuthGuard>
