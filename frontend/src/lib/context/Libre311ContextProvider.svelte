
<script lang="ts">
	import LibreAlert from '$lib/components/LibreAlert.svelte';
	import { createAlertStore } from './Libre311AlertStore';
	import { createLibre311Context, type Libre311ContextProviderProps } from './Libre311Context';
    let {libreServiceProps, unityAuthServiceProps, recaptchaServiceProps, mode} : Libre311ContextProviderProps = $props();
	const alertStore = createAlertStore();
	const currentAlert = alertStore.currentAlert;
	const libre311Context = $derived(createLibre311Context({ libreServiceProps, unityAuthServiceProps, recaptchaServiceProps, mode, ...alertStore }));
</script>

<svelte:head>
	<script
		src="https://www.google.com/recaptcha/enterprise.js?render={recaptchaServiceProps.recaptchaKey}"
	></script>
</svelte:head>

<slot {libre311Context} />

{#if $currentAlert}
	<LibreAlert
		on:close={() => alertStore.close()}
		type={$currentAlert.type}
		title={$currentAlert.title}
		description={$currentAlert.description}
	></LibreAlert>
{/if}
